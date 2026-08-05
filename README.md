<div align="center">

# Flowbit

### Трекер привычек, который не мешает жить

[![Android](https://img.shields.io/badge/Android-5.0+-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Jetpack_Compose-Material_3-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![CI](https://img.shields.io/github/actions/workflow/status/keshtoim/Flowbit/build.yml?style=for-the-badge&label=CI)](https://github.com/keshtoim/Flowbit/actions)
[![License](https://img.shields.io/badge/License-MIT-yellow?style=for-the-badge)](LICENSE)

**[🇷🇺 Русский](README.md) · [🇬🇧 English](README.en.md)**

</div>

---

## Зачем ещё один трекер?

Большинство приложений для привычек — либо слишком простые, либо перегруженные платными функциями. Flowbit занимает середину: **всё, что нужно для серьёзного трекинга** — без подписок, без рекламы, без облака.

- 📴 **100% офлайн** — ваши данные хранятся только на устройстве
- 🎨 **Material You** — виджеты и интерфейс меняют цвет под ваши обои
- ⚡ **Быстро** — галочка за одно касание, виджет на рабочем столе
- 🔓 **Открытый код** — без скрытых трекеров и аналитики

---

## Возможности

<table>
<tr>
<td width="50%">

### ✅ Гибкие привычки
- Счётчик повторений (+ / −) с целью на день
- Частота: каждый день или выбранные дни
- **Числовые цели** — «выпить 2 л воды», «пробежать 5 км»
- **Таймер** — готовые пресеты или произвольное время, отмечает сам
- Периодические цели — «4 раза в неделю»
- **🚫 Табу-привычки** — «не курить»: выполнена по умолчанию, отметь срыв
- Цветные теги и группировка

</td>
<td width="50%">

### 📊 Аналитика
- 🔥 Текущая и лучшая серия
- 📈 График активности за 30 дней
- 🗓️ Тепловая карта за год
- ✅ Процент выполнения: топ-привычка, сложная, средний %
- 🔵 Анализ по периоду — 3 дня / неделя / месяц (связанные кружки)
- 📅 **Экран итогов недели** — 7 точек по каждой привычке

</td>
</tr>
<tr>
<td>

### 🔔 Умные напоминания
- Точные уведомления в нужное время
- Кнопка **«Выполнено ✓»** прямо в шторке — без открытия приложения
- Восстановление после перезагрузки
- **Напоминание при неактивности** — уведомление если не открывал > 25 ч

</td>
<td>

### 🪟 Виджеты
- 📅 Недельный — все привычки за 7 дней
- 🔵 Сводка дня — круговой прогресс
- ➕ Одна привычка — счётчик + кнопка **«+»**
- 🎨 Адаптируются под тему Material You

</td>
</tr>
<tr>
<td>

### 💡 Детали, которые важны
- ⏭ Пропуск дня — намеренно, с подтверждением отмены
- 📝 Заметка к каждому дню + **история всех заметок**
- 🖼️ Фото-баннер (с кадрированием)
- 🎵 Аудио-мотивация
- ↕️ Перетаскивание карточек для сортировки
- 🗑️ Удаление с подтверждением

</td>
<td>

### 🌗 Персонализация
- Тёмная / светлая тема
- 16 цветов акцента + произвольный по HEX-коду
- Эмодзи-аватарка
- Группировка по тегам / частоте / статусу
- 💾 Экспорт и импорт данных в JSON (включая напоминания)

</td>
</tr>
</table>

---

## Стек технологий

![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?logo=kotlin&logoColor=white)
![Compose](https://img.shields.io/badge/Jetpack_Compose-4285F4?logo=jetpackcompose&logoColor=white)
![Material3](https://img.shields.io/badge/Material_3-757575?logo=materialdesign&logoColor=white)
![Room](https://img.shields.io/badge/Room_DB-v11-3DDC84?logo=android&logoColor=white)
![Hilt](https://img.shields.io/badge/Hilt_DI-F6891F)
![Glance](https://img.shields.io/badge/Glance_Widgets-4285F4?logo=android&logoColor=white)
![WorkManager](https://img.shields.io/badge/WorkManager-3DDC84?logo=android&logoColor=white)
![Coil](https://img.shields.io/badge/Coil-FF6B35)

| Слой | Технология |
|---|---|
| UI | Jetpack Compose + Material 3 |
| Виджеты | Jetpack Glance 1.1 · `PreferencesGlanceStateDefinition` |
| DI | Hilt |
| БД | Room 2.6 · SQLite · миграции v1→v11 |
| Изображения | Coil 2.6 + android-image-cropper |
| Фон | AlarmManager · WorkManager |
| Настройки | DataStore Preferences |
| Навигация | Navigation Compose 2.7 |
| Drag-and-drop | sh.calvin.reorderable 2.4 |
| CI/CD | GitHub Actions · auto versionName по числу коммитов |

---

## Быстрый старт

```bash
git clone https://github.com/keshtoim/Flowbit.git
cd Flowbit
./gradlew assembleDebug
```

> Требования: Android Studio Ladybug+, JDK 17, Android 5.0+ (API 21)

APK → `app/build/outputs/apk/debug/app-debug.apk`

---

## Архитектура

```
app/
├── data/
│   ├── database/       # Room: сущности, DAO, миграции (v1→v11)
│   ├── receiver/       # AlarmManager BroadcastReceiver-ы
│   ├── repository/     # Реализации репозиториев
│   └── worker/         # WorkManager: InactivityCheckWorker
├── domain/
│   ├── model/          # Habit, HabitEntry, HabitTag…
│   ├── repository/     # Интерфейсы
│   └── usecase/        # Бизнес-логика
├── presentation/
│   ├── habits/         # Список · редактор · детали
│   ├── statistics/     # Графики и тепловая карта
│   ├── weekly/         # Экран итогов недели
│   ├── settings/       # Настройки · бекап · язык
│   └── theme/          # Material You + тёмная тема
└── widget/             # 3 Glance-виджета + IncrementHabitAction
```

Clean Architecture: `presentation → domain ← data`. ViewModel не знает про БД, UseCase не знает про Compose.

---

## Разрешения

| Разрешение | Зачем |
|---|---|
| `POST_NOTIFICATIONS` | Уведомления-напоминания (Android 13+) |
| `SCHEDULE_EXACT_ALARM` | Точное время напоминания |
| `RECEIVE_BOOT_COMPLETED` | Восстановление будильников после перезагрузки |
| `READ_MEDIA_IMAGES` | Выбор фото из галереи |
| `READ_MEDIA_AUDIO` | Выбор аудиофайла |

---

<div align="center">

MIT License · сделано с ❤️ by [Keshtoim](https://github.com/keshtoim)

</div>
