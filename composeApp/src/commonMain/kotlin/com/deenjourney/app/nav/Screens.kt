package com.deenjourney.app.nav

import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.deenjourney.app.feature.family.*
import com.deenjourney.app.feature.home.*
import com.deenjourney.app.feature.start.*

/** Registers a typed destination and hands the decoded route to [content]. */
inline fun <reified T : R> NavGraphBuilder.screen(noinline content: @Composable (T) -> Unit) {
    composable<T> { e: NavBackStackEntry -> content(e.toRoute<T>()) }
}

fun NavGraphBuilder.registerScreens() {
    // A · start
    screen<Splash> { SplashScreen() }
    screen<Welcome> { WelcomeScreen() }
    screen<SignIn> { SignInScreen() }
    screen<SignUp> { SignUpScreen() }
    screen<Forgot> { ForgotScreen() }
    screen<CheckEmail> { CheckEmailScreen(it.email) }
    screen<PrayerSetup> { PrayerSetupScreen(it.fromSettings) }
    screen<NotifSetup> { NotifSetupScreen() }
    // B · family
    screen<FamilySetup> { FamilySetupScreen(it.onboarding) }
    screen<AgeLevel> { AgeLevelScreen(it.profileId, it.onboarding) }
    screen<Goals> { GoalsScreen(it.onboarding) }
    screen<AddMember> { AddMemberScreen(it.kind, it.profileId) }
    screen<AllSet> { AllSetScreen() }
    // C · hubs
    screen<Home> { HomeScreen() }
    screen<Notifications> { NotificationsScreen() }
    screen<LearnHub> { LearnHubScreen() }
    screen<WorshipHub> { WorshipHubScreen() }
    screen<More> { MoreScreen() }
    screen<Search> { SearchScreen(it.q) }
    // G · family & kids
    screen<KidsSmall> { KidsSmallScreen() }
    screen<KidsHome> { KidsHomeScreen() }
    screen<ParentDashboard> { ParentDashboardScreen() }
    screen<ChildSafe> { ChildSafeScreen(it.profileId) }

    registerQuran()
    registerWorship()
    registerLearn()
    registerTools()
    registerAccount()
}
