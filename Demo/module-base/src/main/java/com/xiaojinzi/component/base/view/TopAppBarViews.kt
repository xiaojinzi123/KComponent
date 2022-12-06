package com.xiaojinzi.component.base.view

import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.xiaojinzi.component.base.R
import com.xiaojinzi.support.bean.StringItemDto
import com.xiaojinzi.support.compose.util.contentWithComposable
import com.xiaojinzi.support.ktx.nothing
import com.xiaojinzi.support.ktx.toStringItemDto
import com.xiaojinzi.support.ktx.tryFinishActivity

@Composable
fun AppbarNormal(
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colors.primarySurface,
    contentColor: Color = contentColorFor(backgroundColor),
    backIconColor: Color = contentColor,
    @DrawableRes backIconRsd: Int? = R.drawable.res_back1,
    backClickListener: (() -> Unit)? = null,
    title: StringItemDto? = null,
    titleAlign: TextAlign = TextAlign.Start,
    menu1IconRsd: Int? = null,
    menu1Text: @Composable (() -> Unit)? = null,
    menu1ClickListener: (() -> Unit)? = null,
    menu2IconRsd: Int? = null,
    menu2ClickListener: (() -> Unit)? = null,
    menu3IconRsd: Int? = null,
    menu3ClickListener: (() -> Unit)? = null,
) {
    val context = LocalContext.current
    val defaultBackListener: () -> Unit = {
        context.tryFinishActivity()
    }
    val targetBackListener = backClickListener ?: defaultBackListener
    TopAppBar(
        modifier = modifier,
        backgroundColor = backgroundColor,
        contentColor = contentColor,
        contentPadding = WindowInsets.statusBars.asPaddingValues(),
        elevation = 0.dp,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            backIconRsd?.let { backIconRsd ->
                IconButton(onClick = targetBackListener) {
                    Icon(
                        modifier = Modifier.size(20.dp).nothing(),
                        painter = painterResource(id = backIconRsd),
                        tint = backIconColor,
                        contentDescription = null,
                    )
                }
            }
            Spacer(modifier = Modifier.size(width = 12.dp, height = 0.dp))
            Text(
                modifier = Modifier
                    .weight(weight = 1f, fill = true)
                    .padding(end = 16.dp),
                text = title?.contentWithComposable() ?: "",
                textAlign = titleAlign,
                style = MaterialTheme.typography.subtitle1.copy(
                    color = contentColor,
                )
            )
            if (menu3IconRsd == null) {
                Spacer(modifier = Modifier.width(width = 0.dp))
            } else {
                IconButton(
                    onClick = {
                        menu3ClickListener?.invoke()
                    }
                ) {
                    Icon(
                        modifier = Modifier.size(24.dp),
                        painter = painterResource(id = menu3IconRsd),
                        contentDescription = null
                    )
                }
            }
            if (menu2IconRsd == null) {
                Spacer(modifier = Modifier.width(width = 0.dp))
            } else {
                IconButton(
                    onClick = {
                        menu2ClickListener?.invoke()
                    }
                ) {
                    Icon(
                        modifier = Modifier.size(24.dp),
                        painter = painterResource(id = menu2IconRsd),
                        contentDescription = null
                    )
                }
            }
            if (menu1IconRsd == null && menu1Text == null) {
                Spacer(modifier = Modifier.width(width = 24.dp))
            } else {
                menu1IconRsd?.let {
                    IconButton(
                        onClick = {
                            menu1ClickListener?.invoke()
                        }
                    ) {
                        Icon(
                            modifier = Modifier.size(24.dp),
                            painter = painterResource(id = menu1IconRsd),
                            contentDescription = null
                        )
                    }
                }
                menu1Text?.let {
                    Box(
                        modifier = Modifier
                            .wrapContentSize()
                            .clip(shape = RoundedCornerShape(size = 2.dp))
                            .clickable {
                                menu1ClickListener?.invoke()
                            }
                            .padding(horizontal = 2.dp, vertical = 2.dp)
                            .nothing(),
                    ) {
                        menu1Text()
                    }

                }
            }
            Spacer(modifier = Modifier.width(width = 8.dp))
        }
    }
}

@Preview
@Composable
private fun AppbarNormalPreview() {
    AppbarNormal(
        title = "测试".toStringItemDto(),
        menu1IconRsd = R.drawable.res_back1,
    )
}