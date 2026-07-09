<div align="center">

# 〰️ Flowbit

**Минималистичный трекер привычек для Android**

[![Android](https://img.shields.io/badge/Platform-Android-3DDC84?logo=android&logoColor=white)](https://android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Jetpack_Compose-Material_3-4285F4?logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![CI](https://github.com/keshtoim/Flowbit/actions/workflows/build.yml/badge.svg)](https://github.com/keshtoim/Flowbit/actions)

</div>

---

## Возможности

### Привычки
| | |
|---|---|
| 🎯 | Счётчик повторений в день с кнопками + и − |
| 📅 | Частота: ежедневно или по выбранным дням недели |
| 🏷️ | Цветные теги и группировка (по тегам / частоте / статусу) |
| 📆 | Цель на период — «N раз в неделю / в месяц» |
| 📝 | Заметка к каждому дню выполнения |
| 🖼️ | Фото с кадрированием; можно скрыть на общем экране |
| 🎵 | Аудиофайл с воспроизведением прямо в приложении |

### Аналитика
| | |
|---|---|
| 🔥 | Текущая и лучшая серия |
| 📊 | График активности за 30 дней |
| 🗓️ | Тепловая карта за год |
| ✅ | Процент выполнения |

### Виджеты
| Виджет | Размер | Описание |
|---|---|---|
| Недельный | 3×2 | Все привычки за 7 дней, ✓ в выполненных |
| Сводка дня | 2×2 | Круговой прогресс X/Y |
| Одна привычка | 2×2 | Эмодзи + счётчик + серия 🔥; привычка выбирается при добавлении |

### Остальное
| | |
|---|---|
| 🔔 | Точные напоминания (AlarmManager); в тексте — эмодзи и название привычки |
| 💬 | Кнопка «Не могу сегодня» → случайная мотивационная цитата |
| 🌗 | Светлая и тёмная тема (Material Design 3) |
| 💾 | Экспорт / импорт данных в JSON |
| ↕️ | Ручная сортировка привычек в настройках |

---

## Стек технологий

| Слой | Технология |
|---|---|
| UI | Jetpack Compose + Material 3 |
| Виджеты | Jetpack Glance 1.1 · `SizeMode.Exact` · `PreferencesGlanceStateDefinition` |
| DI | Hilt |
| БД | Room 2.6 · SQLite · v7 |
| Изображения | Coil 2.6 · android-image-cropper 4.7 (`com.vanniktech`) |
| Аудио | `android.media.MediaPlayer` |
| Фон | AlarmManager · WorkManager |
| Настройки | DataStore Preferences |
| Навигация | Navigation Compose 2.7 |
| CI/CD | GitHub Actions · auto versionName по числу коммитов |

---

## Сборка

> Требования: Android Studio Ladybug+, JDK 17

```bash
git clone https://github.com/keshtoim/Flowbit.git
cd Flowbit
./gradlew assembleDebug
```

APK → `app/build/outputs/apk/debug/app-debug.apk`

> `versionCode` и `versionName` (`1.0.<N>`) считаются автоматически из числа git-коммитов.

---

## Архитектура

```
app/
├── data/
│   ├── database/       # Room: сущности, DAO, миграции (v1→v7)
│   └── repository/     # Реализации репозиториев
├── domain/
│   ├── model/          # Habit, HabitTag, HabitEntry, HabitStats…
│   ├── repository/     # Интерфейсы
│   └── usecase/        # Бизнес-логика
├── presentation/
│   ├── habits/         # Список · редактор · детали
│   ├── today/          # Экран «Сегодня»
│   ├── statistics/     # Статистика и графики
│   ├── settings/       # Настройки · бекап
│   ├── navigation/     # NavGraph с анимациями
│   └── theme/          # M3 цвета и типографика
└── widget/             # 3 Glance-виджета + конфиг-активити
```

---

## Разрешения

| Разрешение | Зачем |
|---|---|
| `POST_NOTIFICATIONS` | Уведомления-напоминания (Android 13+) |
| `SCHEDULE_EXACT_ALARM` | Точное время напоминания |
| `RECEIVE_BOOT_COMPLETED` | Восстановление будильников после перезагрузки |
| `READ_MEDIA_IMAGES` | Выбор фото из галереи (Android 13+) |
| `READ_MEDIA_AUDIO` | Выбор аудиофайла из галереи (Android 13+) |

---

## Скриншоты

> Добавьте скриншоты в `docs/screenshots/` и вставьте их здесь.

---

<div align="center">

MIT License · made with ❤️ by [Keshtoim](https://github.com/keshtoim)

</div>
