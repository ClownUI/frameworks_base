/*
 * Copyright (C) 2023 ClownUI
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.systemui.clown

import com.android.systemui.qs.tileimpl.QSTileImpl
import com.android.systemui.qs.tiles.CaffeineTile

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import dagger.multibindings.StringKey

@Module
interface ClownModule {
    /** Inject CaffeineTile into tileMap in QSModule */
    @Binds
    @IntoMap
    @StringKey(CaffeineTile.TILE_SPEC)
    fun bindCaffeineTile(caffeineTile: CaffeineTile): QSTileImpl<*>
}
