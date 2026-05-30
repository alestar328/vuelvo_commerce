package com.delta.vuelvo_commerce.ui.biz

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.delta.vuelvo_commerce.R
import com.delta.vuelvo_commerce.ui.theme.VuAccentDeep
import com.delta.vuelvo_commerce.ui.theme.VuInk
import com.delta.vuelvo_commerce.ui.theme.VuInk2
import com.delta.vuelvo_commerce.ui.theme.VuInk3

/** Brand row + big title — mirror of BizHeader in vuelvo-biz-config.jsx. */
@Composable
fun BizHeader(
    title: String,
    subtitle: String? = null,
    right: (@Composable () -> Unit)? = null,
) {
    Column(Modifier.padding(top = 58.dp, bottom = 16.dp)) {
        Row(
            Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                Image(
                    painter = painterResource(R.drawable.ic_vuelvo_logo),
                    contentDescription = null,
                    modifier = Modifier.size(26.dp).clip(RoundedCornerShape(7.dp)),
                )
                androidx.compose.material3.Text(
                    buildAnnotatedString {
                        withStyle(SpanStyle(fontWeight = FontWeight.ExtraBold)) { append("Vuelvo") }
                        withStyle(SpanStyle(fontWeight = FontWeight.ExtraBold, color = VuAccentDeep)) { append(" Comercios") }
                    },
                    fontSize = 16.sp,
                    letterSpacing = (-0.3).sp,
                    color = VuInk,
                )
            }
            right?.invoke()
        }
        androidx.compose.material3.Text(
            title,
            fontSize = 32.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = (-0.9).sp,
            color = VuInk,
        )
        if (subtitle != null) {
            Spacer(Modifier.size(5.dp))
            androidx.compose.material3.Text(
                subtitle,
                fontSize = 14.5.sp,
                color = VuInk2,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

/** Uppercase field caption with optional right-aligned hint. */
@Composable
fun FieldLabel(text: String, hint: String? = null) {
    Row(
        Modifier.fillMaxWidth().padding(bottom = 9.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom,
    ) {
        androidx.compose.material3.Text(
            text.uppercase(),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = VuInk2,
            letterSpacing = 0.4.sp,
        )
        if (hint != null) {
            androidx.compose.material3.Text(
                hint,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = VuInk3,
            )
        }
    }
}
