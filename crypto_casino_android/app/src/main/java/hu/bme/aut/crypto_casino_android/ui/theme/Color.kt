package hu.bme.aut.crypto_casino_android.ui.theme

import androidx.compose.ui.graphics.Color

// Dracula Theme Colors - Official Palette
// https://draculatheme.com/contribute

// Primary colors - Dracula Purple
val PrimaryDark = Color(0xFF6E40AA)   // Darker Purple
val Primary = Color(0xFFBD93F9)       // Dracula Purple
val PrimaryLight = Color(0xFFE7CCFF) // Lighter Purple

// Secondary colors - Dracula Cyan
val Secondary = Color(0xFF8BE9FD)     // Dracula Cyan
val SecondaryDark = Color(0xFF4DB8D0) // Darker Cyan
val SecondaryLight = Color(0xFFCAF7FF) // Lighter Cyan

// Tertiary colors - Dracula Pink
val Tertiary = Color(0xFFFF79C6)      // Dracula Pink
val TertiaryDark = Color(0xFFE53FA2)  // Darker Pink
val TertiaryLight = Color(0xFFFFB3E0) // Lighter Pink

// Background colors - Dracula Dark
val Background = Color(0xFF282A36)    // Dracula Background
val Surface = Color(0xFF44475A)       // Dracula Current Line
val SurfaceVariant = Color(0xFF6272A4) // Dracula Comment

// Text colors - Dracula Foreground
val OnBackground = Color(0xFFF8F8F2)  // Dracula Foreground
val OnSurface = Color(0xFFF8F8F2)     // Dracula Foreground
val OnSurfaceVariant = Color(0xFFBDBFC9) // Lighter variant
val OnDisabled = Color(0xFF6272A4)    // Dracula Comment

// Status colors - Dracula Theme
val Success = Color(0xFF50FA7B)       // Dracula Green
val Error = Color(0xFFFF5555)         // Dracula Red
val Warning = Color(0xFFFFB86C)       // Dracula Orange
val Info = Color(0xFF8BE9FD)          // Dracula Cyan

// Transaction type colors - Dracula Theme
val Purple = Color(0xFFBD93F9)        // Dracula Purple for TOKEN_PURCHASED
val Amber = Color(0xFFFFB86C)         // Dracula Orange for TOKEN_EXCHANGED
val Bet = Color(0xFF8BE9FD)           // Dracula Cyan for BET
val Win = Color(0xFFF1FA8C)           // Dracula Yellow for WIN

// Additional Dracula colors for specific use cases
val DraculaGold = Color(0xFFF1FA8C)   // Dracula Yellow (for lucky 7s, etc.)
val DraculaReelBackground = Color(0xFF21222C) // Darker than background for reel displays
val DraculaCardBackground = Color(0xFF383A59) // Between Surface and Background

// Dracula Light Theme Colors - Inverted palette for light mode
// Background and surface colors are light, but accent colors remain vibrant

// Primary colors - Light mode
val PrimaryLightMode = Color(0xFF6E40AA)   // Darker purple for light bg
val PrimaryDarkLightMode = Color(0xFF4A2879) // Even darker purple
val PrimaryLightLightMode = Color(0xFFBD93F9) // Lighter purple for containers

// Secondary colors - Light mode
val SecondaryLightMode = Color(0xFF1A7A8F)  // Darker cyan for readability
val SecondaryDarkLightMode = Color(0xFF0D5468) // Darker cyan
val SecondaryLightLightMode = Color(0xFF8BE9FD) // Lighter cyan for containers

// Tertiary colors - Light mode
val TertiaryLightMode = Color(0xFFD4428E)   // Darker pink for readability
val TertiaryDarkLightMode = Color(0xFFA02F6A) // Even darker pink
val TertiaryLightLightMode = Color(0xFFFF79C6) // Lighter pink for containers

// Background colors - Light mode (inverted)
val BackgroundLight = Color(0xFFF8F8F2)     // Light cream/white (Dracula foreground as background)
val SurfaceLight = Color(0xFFFFFFFF)        // Pure white for elevated surfaces
val SurfaceVariantLight = Color(0xFFE5E5E0) // Slightly darker for variants

// Text colors - Light mode
val OnBackgroundLight = Color(0xFF282A36)   // Dark text on light bg
val OnSurfaceLight = Color(0xFF282A36)      // Dark text on light surface
val OnSurfaceVariantLight = Color(0xFF44475A) // Slightly lighter dark text
val OnDisabledLight = Color(0xFF6272A4)     // Muted text

// Status colors - Light mode (slightly adjusted for contrast)
val SuccessLight = Color(0xFF2FB358)        // Darker green for light bg
val ErrorLight = Color(0xFFE02F2F)          // Darker red for light bg
val WarningLight = Color(0xFFE89339)        // Darker orange for light bg
val InfoLight = Color(0xFF1A7A8F)           // Darker cyan for light bg

// Additional colors for light mode
val DraculaGoldLight = Color(0xFFD4C021)    // Darker yellow for light bg
val DraculaReelBackgroundLight = Color(0xFFE5E5E0) // Light gray for reel displays
val DraculaCardBackgroundLight = Color(0xFFF0F0EB) // Very light for cards

// Transaction type colors - Light mode
val PurpleLight = Color(0xFF6E40AA)        // Darker purple for TOKEN_PURCHASED
val AmberLight = Color(0xFFE89339)         // Darker orange for TOKEN_EXCHANGED
val BetLight = Color(0xFF1A7A8F)           // Darker cyan for BET
val WinLight = Color(0xFFD4C021)           // Darker yellow for WIN
