package com.vanta.app.ui.components

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.AwaitPointerEventScope
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.onClick as semanticsOnClick
import androidx.compose.ui.semantics.onLongClick as semanticsOnLongClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.util.fastAny

/**
 * Окно ожидания перед стартом по касанию. Если за это время палец не ушёл дальше touch slop
 * и жест не забрал родитель (скролл списка), действие запускается, не дожидаясь отпускания.
 */
const val INSTANT_TAP_WINDOW_MS = 60L

private enum class TapOutcome { UP, MOVED, CANCELLED }

/**
 * Действие «как в iOS»: срабатывает на касании, а не на отпускании.
 *
 * - Быстрый тап (палец поднят раньше окна) срабатывает сразу при отпускании.
 * - Если палец лежит [INSTANT_TAP_WINDOW_MS] без движения, [onTap] срабатывает сразу, палец ещё на экране.
 * - Если палец за окно ушёл дальше touch slop или жест забрал скролл, ничего не запускается.
 * - Долгое нажатие ([onLongClick]) срабатывает по системному таймауту, если палец остался на месте.
 * - [onPressStart] вызывается в момент касания (годится для лёгкого прогрева), до решения о запуске.
 */
fun Modifier.instantTap(
    interactionSource: MutableInteractionSource,
    onPressStart: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    onTap: () -> Unit
): Modifier = composed {
    val tapState = rememberUpdatedState(onTap)
    val longState = rememberUpdatedState(onLongClick)
    val startState = rememberUpdatedState(onPressStart)

    this
        .semantics(mergeDescendants = true) {
            role = Role.Button
            semanticsOnClick(label = null) {
                tapState.value()
                true
            }
            if (longState.value != null) {
                semanticsOnLongClick(label = null) {
                    longState.value?.invoke()
                    true
                }
            }
        }
        .pointerInput(interactionSource) {
            awaitEachGesture {
                val down = awaitFirstDown()
                // Если в этот момент список ещё катится и родитель забрал касание (остановка fling), это не тап.
                val downFinal = awaitPointerEvent(PointerEventPass.Final)
                if (downFinal.changes.fastAny { it.isConsumed }) return@awaitEachGesture

                startState.value?.invoke()

                val slop = viewConfiguration.touchSlop
                val first = withTimeoutOrNull(INSTANT_TAP_WINDOW_MS) {
                    awaitEndOrMove(down.id, down.position, slop)
                }
                if (first == TapOutcome.MOVED || first == TapOutcome.CANCELLED) return@awaitEachGesture

                // Здесь: либо быстрый тап (UP), либо палец лежит неподвижно (first == null).
                val press = PressInteraction.Press(down.position)
                interactionSource.tryEmit(press)
                var end: PressInteraction = PressInteraction.Cancel(press)
                try {
                    tapState.value()
                    if (first == TapOutcome.UP) {
                        end = PressInteraction.Release(press)
                        return@awaitEachGesture
                    }

                    val longCb = longState.value
                    val tail: TapOutcome? = if (longCb != null) {
                        val remain = (viewConfiguration.longPressTimeoutMillis - INSTANT_TAP_WINDOW_MS)
                            .coerceAtLeast(0L)
                        withTimeoutOrNull(remain) { awaitEndOrMove(down.id, down.position, slop) }
                    } else {
                        awaitEndOrMove(down.id, down.position, slop)
                    }
                    when (tail) {
                        null -> {
                            // Палец удержан дольше порога: открываем меню (трек уже играет).
                            end = PressInteraction.Release(press)
                            longCb?.invoke()
                        }
                        TapOutcome.UP -> end = PressInteraction.Release(press)
                        else -> Unit // Начался скролл: просто снимаем нажатие.
                    }
                } finally {
                    interactionSource.tryEmit(end)
                }
            }
        }
}

/**
 * Ждёт, чем закончится касание: отпускание, уход пальца за touch slop
 * или забранный родителем жест (скролл). Сначала событие видит ребёнок (Main),
 * потом проверяем Final, чтобы заметить, что родитель его потребил.
 */
private suspend fun AwaitPointerEventScope.awaitEndOrMove(
    pointerId: PointerId,
    start: Offset,
    slop: Float
): TapOutcome {
    while (true) {
        val event = awaitPointerEvent(PointerEventPass.Main)
        val change = event.changes.firstOrNull { it.id == pointerId } ?: return TapOutcome.CANCELLED
        if (change.isConsumed) return TapOutcome.CANCELLED
        if (!change.pressed) return TapOutcome.UP
        if ((change.position - start).getDistance() > slop) return TapOutcome.MOVED
        val final = awaitPointerEvent(PointerEventPass.Final)
        if (final.changes.fastAny { it.isConsumed }) return TapOutcome.CANCELLED
    }
}
