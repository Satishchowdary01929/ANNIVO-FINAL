package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.AnnivoSaffron

@Composable
fun AnnivoBrandLogo(
    modifier: Modifier = Modifier,
    isDarkBg: Boolean = false,
    iconSize: Dp = 64.dp,
    showSubtitles: Boolean = true
) {
    val textColor = if (isDarkBg) Color.White else MaterialTheme.colorScheme.onBackground
    val subtitleColor = if (isDarkBg) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Standalone Logo Icon
        Image(
            painter = painterResource(id = R.drawable.ic_annivo_logo_icon),
            contentDescription = "Annivo Brand Icon",
            modifier = Modifier
                .size(iconSize)
                .padding(4.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Brand Name Text: "Anni" (Navy/White) + "vo" (Saffron)
        val nameFontSize = (iconSize.value * 0.4f).sp
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(color = textColor, fontWeight = FontWeight.Bold)) {
                        append("Anni")
                    }
                    withStyle(style = SpanStyle(color = AnnivoSaffron, fontWeight = FontWeight.Bold)) {
                        append("vo")
                    }
                },
                fontSize = nameFontSize,
                letterSpacing = 0.5.sp
            )
        }

        if (showSubtitles) {
            Spacer(modifier = Modifier.height(4.dp))
            // Subtitle: — Good Food. One Community. —
            val sub1Val = iconSize.value * 0.15f
            val sub1Size = (if (sub1Val < 10f) 10f else sub1Val).sp
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                Text(
                    text = "—  Good Food. One Community.  —",
                    color = subtitleColor,
                    fontSize = sub1Size,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun AnnivoHorizontalLogo(
    modifier: Modifier = Modifier,
    isDarkBg: Boolean = false,
    iconSize: Dp = 40.dp
) {
    val textColor = if (isDarkBg) Color.White else MaterialTheme.colorScheme.onBackground

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_annivo_logo_icon),
            contentDescription = "Annivo Icon",
            modifier = Modifier.size(iconSize)
        )
        Spacer(modifier = Modifier.width(8.dp))
        
        val logoTextVal = iconSize.value * 0.55f
        val logoTextSize = (if (logoTextVal < 18f) 18f else logoTextVal).sp
        
        Text(
            text = buildAnnotatedString {
                withStyle(style = SpanStyle(color = textColor, fontWeight = FontWeight.Black)) {
                    append("Anni")
                }
                withStyle(style = SpanStyle(color = AnnivoSaffron, fontWeight = FontWeight.Black)) {
                    append("vo")
                }
            },
            fontSize = logoTextSize,
            letterSpacing = 1.sp
        )
    }
}
