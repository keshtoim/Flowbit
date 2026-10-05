package com.flowbit.app.presentation.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

private data class OnboardingPage(
    val emoji: String,
    val title: String,
    val subtitle: String,
)

private val pages = listOf(
    OnboardingPage(
        emoji = "🌱",
        title = "Маленькие шаги — большие результаты",
        subtitle = "Добавь привычку, отмечай выполнение каждый день и наблюдай, как она становится частью жизни.",
    ),
    OnboardingPage(
        emoji = "📊",
        title = "Следи за прогрессом",
        subtitle = "Серии дней, тепловая карта года, инсайты по дням недели — всё чтобы понять себя лучше.",
    ),
    OnboardingPage(
        emoji = "🚀",
        title = "Выбери первую привычку",
        subtitle = "Начни с чего-то простого. Маленькая победа сегодня — фундамент на завтра.",
    ),
)

private data class HabitTemplate(val emoji: String, val name: String)

private val templates = listOf(
    HabitTemplate("🏃", "Пробежка"),
    HabitTemplate("💧", "Вода 8 стаканов"),
    HabitTemplate("📚", "Чтение 20 мин"),
    HabitTemplate("🧘", "Медитация"),
    HabitTemplate("💊", "Витамины"),
    HabitTemplate("🛏️", "Ранний подъём"),
    HabitTemplate("✍️", "Дневник"),
    HabitTemplate("🚶", "10 000 шагов"),
    HabitTemplate("🥗", "Без сахара"),
    HabitTemplate("💪", "Тренировка"),
    HabitTemplate("🌙", "До 23:00 спать"),
    HabitTemplate("🚭", "Без сигарет"),
)

@Composable
fun OnboardingScreen(
    onFinish: (templateName: String?, templateEmoji: String?) -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    var page by remember { mutableStateOf(0) }
    var selectedTemplate by remember { mutableStateOf<HabitTemplate?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // ── Индикаторы страниц ────────────────────────────────────────────
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.align(Alignment.CenterHorizontally),
            ) {
                repeat(pages.size) { i ->
                    Box(
                        modifier = Modifier
                            .size(if (i == page) 24.dp else 8.dp, 8.dp)
                            .clip(CircleShape)
                            .background(
                                if (i == page) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                            ),
                    )
                }
            }

            Spacer(Modifier.height(40.dp))

            // ── Содержимое слайда ─────────────────────────────────────────────
            AnimatedContent(
                targetState = page,
                transitionSpec = {
                    (slideInHorizontally(tween(300)) { it } + fadeIn(tween(300))) togetherWith
                        (slideOutHorizontally(tween(300)) { -it } + fadeOut(tween(300)))
                },
                modifier = Modifier.weight(1f),
                label = "onboarding_page",
            ) { currentPage ->
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = pages[currentPage].emoji,
                        fontSize = 80.sp,
                    )
                    Spacer(Modifier.height(24.dp))
                    Text(
                        text = pages[currentPage].title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = pages[currentPage].subtitle,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f),
                    )

                    // ── Шаблоны привычек на последнем слайде ─────────────────
                    if (currentPage == 2) {
                        Spacer(Modifier.height(24.dp))
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            contentPadding = PaddingValues(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            items(templates) { t ->
                                val isSelected = selectedTemplate == t
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedTemplate = if (isSelected) null else t },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected)
                                            MaterialTheme.colorScheme.primaryContainer
                                        else
                                            MaterialTheme.colorScheme.surfaceVariant,
                                    ),
                                    elevation = CardDefaults.cardElevation(if (isSelected) 4.dp else 0.dp),
                                ) {
                                    Column(
                                        modifier = Modifier.padding(8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                    ) {
                                        Text(text = t.emoji, fontSize = 28.sp)
                                        Spacer(Modifier.height(4.dp))
                                        Text(
                                            text = t.name,
                                            style = MaterialTheme.typography.labelSmall,
                                            textAlign = TextAlign.Center,
                                            maxLines = 2,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // ── Кнопки навигации ──────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (page > 0) {
                    TextButton(onClick = { page-- }) { Text("Назад") }
                } else {
                    Spacer(Modifier.size(1.dp))
                }

                if (page < pages.size - 1) {
                    Button(onClick = { page++ }) { Text("Далее") }
                } else {
                    Button(
                        onClick = {
                            viewModel.markShown()
                            onFinish(selectedTemplate?.name, selectedTemplate?.emoji)
                        },
                    ) {
                        Text(if (selectedTemplate != null) "Начать с «${selectedTemplate!!.name}»" else "Начать")
                    }
                }
            }
        }
    }
}
