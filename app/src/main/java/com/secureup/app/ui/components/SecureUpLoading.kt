package com.secureup.app.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.secureup.app.R
import com.secureup.app.ui.theme.SecureUPTheme
import com.secureup.app.ui.theme.SecureUpColors
import com.secureup.app.ui.theme.Spacing

@Composable
fun SecureUpLoading(
    modifier: Modifier = Modifier,
    size: Dp = 120.dp,
) {
    val composition by rememberLottieComposition(
        spec = LottieCompositionSpec.RawRes(R.raw.secureup_loading),
    )
    val isPreview = LocalInspectionMode.current
    val progressState by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever,
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
        content = {
            LottieAnimation(
                composition = composition,
                progress = { if (isPreview) 0.5f else progressState },
                modifier = Modifier.size(size),
            )
        },
    )
}

@Preview(showBackground = true)
@Composable
private fun SecureUpLoadingPreview() {
    SecureUPTheme {
        Surface(color = SecureUpColors.Background) {
            Box(modifier = Modifier.padding(Spacing.L)) {
                SecureUpLoading()
            }
        }
    }
}
