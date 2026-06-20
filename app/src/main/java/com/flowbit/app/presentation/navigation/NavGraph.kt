package com.flowbit.app.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.flowbit.app.presentation.habits.list.HabitListScreen
import com.flowbit.app.presentation.habits.add.AddEditHabitScreen
import com.flowbit.app.presentation.habits.detail.HabitDetailScreen
import com.flowbit.app.presentation.statistics.StatisticsScreen
import com.flowbit.app.presentation.settings.SettingsScreen

sealed class Screen(val route: String) {
    data object HabitList : Screen("habit_list")
    data object AddHabit : Screen("add_habit")
    data object EditHabit : Screen("edit_habit/{habitId}") {
        fun createRoute(habitId: Long) = "edit_habit/$habitId"
    }
    data object HabitDetail : Screen("habit_detail/{habitId}") {
        fun createRoute(habitId: Long) = "habit_detail/$habitId"
    }
    data object Statistics : Screen("statistics")
    data object Settings : Screen("settings")
}

@Composable
fun FlowbitNavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Screen.HabitList.route) {

        composable(Screen.HabitList.route) {
            HabitListScreen(
                onAddHabit = { navController.navigate(Screen.AddHabit.route) },
                onHabitClick = { id -> navController.navigate(Screen.HabitDetail.createRoute(id)) },
                onStatisticsClick = { navController.navigate(Screen.Statistics.route) },
                onSettingsClick = { navController.navigate(Screen.Settings.route) },
            )
        }

        composable(Screen.AddHabit.route) {
            AddEditHabitScreen(
                habitId = null,
                onBack = { navController.popBackStack() },
            )
        }

        composable(
            route = Screen.EditHabit.route,
            arguments = listOf(navArgument("habitId") { type = NavType.LongType }),
        ) { backStackEntry ->
            val habitId = backStackEntry.arguments?.getLong("habitId")
            AddEditHabitScreen(
                habitId = habitId,
                onBack = { navController.popBackStack() },
            )
        }

        composable(
            route = Screen.HabitDetail.route,
            arguments = listOf(navArgument("habitId") { type = NavType.LongType }),
        ) { backStackEntry ->
            val habitId = backStackEntry.arguments?.getLong("habitId") ?: return@composable
            HabitDetailScreen(
                habitId = habitId,
                onBack = { navController.popBackStack() },
                onEdit = { navController.navigate(Screen.EditHabit.createRoute(habitId)) },
            )
        }

        composable(Screen.Statistics.route) {
            StatisticsScreen(
                onBack = { navController.popBackStack() },
                onHabitClick = { id -> navController.navigate(Screen.HabitDetail.createRoute(id)) },
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}
