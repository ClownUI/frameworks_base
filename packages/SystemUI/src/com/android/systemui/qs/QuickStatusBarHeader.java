/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package com.android.systemui.qs;

import static android.app.StatusBarManager.DISABLE2_QUICK_SETTINGS;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import android.annotation.*;
import android.content.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.util.*;
import android.widget.*;
import android.view.*;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.PorterDuff.Mode;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.Space;
import android.widget.TextView;

import com.android.systemui.R;

import com.android.settingslib.Utils;
import com.android.systemui.battery.BatteryMeterView;
import com.android.systemui.Dependency;
import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.statusbar.phone.StatusBarContentInsetsProvider;
import com.android.systemui.statusbar.phone.StatusBarIconController;
import com.android.systemui.statusbar.phone.StatusBarIconController.TintedIconManager;
import com.android.systemui.statusbar.phone.StatusIconContainer;
import com.android.systemui.statusbar.policy.Clock;
import com.android.systemui.statusbar.policy.VariableDateView;
import com.android.systemui.util.LargeScreenUtils;
import com.android.systemui.tuner.TunerService;

import com.evillium.prjct.utils.EvlUtils;

/**
 * View that contains the top-most bits of the QS panel (primarily the status bar with date, time,
 * battery, carrier info and privacy icons) and also contains the {@link QuickQSPanel}.
 */
public class QuickStatusBarHeader extends FrameLayout implements TunerService.Tunable {
            
    private static final String ANCIENT_UI_HEADER_HEIGHT =
            "system:" + "ANCIENT_UI_HEADER_HEIGHT"; 
    private static final String ANCIENT_UI_HEADER_HEIGHT_LAND =
            "system:" + "ANCIENT_UI_HEADER_HEIGHT_LAND"; 
    private static final String ANCIENT_UI_HEADERIMG_STYLE =
            "system:" + "ANCIENT_UI_HEADERIMG_STYLE"; 
    private static final String ANCIENT_UI_HEADERIMG_SET =
            "system:" + "ANCIENT_UI_HEADERIMG_SET";  
    private static final String ANCIENT_UI_HEADERIMG_SWITCH =
            "system:" + "ANCIENT_UI_HEADERIMG_SWITCH";
    private static final String ANCIENT_UI_HEADERIMG_LAND_SWITCH =
            "system:" + "ANCIENT_UI_HEADERIMG_LAND_SWITCH";    
    private static final String ANCIENT_UI_HEADERIMG_ANIMATION =
            "system:" + "ANCIENT_UI_HEADERIMG_ANIMATION";  
    private static final String ANCIENT_UI_HEADERIMG_TINT =
            "system:" + "ANCIENT_UI_HEADERIMG_TINT"; 
    private static final String ANCIENT_UI_HEADERIMG_TINT_CUSTOM =
            "system:" + "ANCIENT_UI_HEADERIMG_TINT_CUSTOM";      
    private static final String ANCIENT_UI_HEADERIMG_ALPHA =
            "system:" + "ANCIENT_UI_HEADERIMG_ALPHA";      
    private static final String ANCIENT_UI_HEADERIMG_USECUSTOMHEIGHT =
            "system:" + "ANCIENT_UI_HEADERIMG_USECUSTOMHEIGHT";

    private static final String IMAGE_HEADER_HEIGHTP =
            "system:" + "IMAGE_HEADER_HEIGHTP"; 
    private static final String IMAGE_HEADER_HEIGHTL =
            "system:" + "IMAGE_HEADER_HEIGHTL";      
    private static final String IMAGE_HEADER_HEIGHTPING =
            "system:" + "IMAGE_HEADER_HEIGHTPING";      
    private static final String IMAGE_HEADER_HEIGHTNDU =
            "system:" + "IMAGE_HEADER_HEIGHTNDU";
    private static final String IMAGE_HEADER_SCALETYPE =
            "system:" + "IMAGE_HEADER_SCALETYPE";
    private static final String IMAGE_HEADER_CLIPOUTLINE =
            "system:" + "IMAGE_HEADER_CLIPOUTLINE";

    private boolean mExpanded;
    private boolean mQsDisabled;

    private TouchAnimator mAnciHeaderimgAnimator;
    
    //headerimg
    private boolean mHeaderImageEnabled;
    private boolean mHeaderImageLandDisabled;
    private boolean mHeaderImageHeightEnabled;
    private boolean digawefull;
    private ImageView mBackgroundImage;
    private View mStatusBarHeaderMachineLayout;
    private View mStatusBarHeaderInnerLayout;
    private int mAncientUIheaderheight;
    private int mAncientUIheaderheightLand;
    private int mAncientUIheaderStyle;
    private int mAncientUIheaderImgStyle;
    private int mAncientUIheaderAniStyle;
    private int mAncientUIheaderAlphaStyle; 
    private int mAncientUIheaderTintStyle;
    private int mAncientUIheaderTintStyleCustom;
    private int mColorAccent; 
    private int mColorTextPrimary; 
    private int mColorPutihIreng;   
    private int mColorWindow; 

    private int jembutheight;  
    private int jembutpanjangheight;  
    private int jembutpinggir;   
    private int jembutduwur; 

    protected QuickQSPanel mHeaderQsPanel;

    public QuickStatusBarHeader(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mHeaderQsPanel = findViewById(R.id.quick_qs_panel);     
        mBackgroundImage = findViewById(R.id.qs_header_image_view);
        mStatusBarHeaderMachineLayout = findViewById(R.id.layout_header);
        mStatusBarHeaderInnerLayout = findViewById(R.id.layout_inner_header);
        mBackgroundImage.setClipToOutline(true);
                
        updateResources();

        Dependency.get(TunerService.class).addTunable(this,
                ANCIENT_UI_HEADERIMG_SWITCH,
                ANCIENT_UI_HEADERIMG_STYLE, 
                ANCIENT_UI_HEADERIMG_SET, 
                ANCIENT_UI_HEADERIMG_TINT,
                ANCIENT_UI_HEADERIMG_TINT_CUSTOM,
                ANCIENT_UI_HEADERIMG_ALPHA,
                ANCIENT_UI_HEADERIMG_USECUSTOMHEIGHT,
                IMAGE_HEADER_HEIGHTP,
                IMAGE_HEADER_HEIGHTL,
                IMAGE_HEADER_HEIGHTPING,
                IMAGE_HEADER_HEIGHTNDU,
                ANCIENT_UI_HEADERIMG_ANIMATION,
                IMAGE_HEADER_SCALETYPE,
                ANCIENT_UI_HEADERIMG_LAND_SWITCH);
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updateResources();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Only react to touches inside QuickQSPanel
        if (event.getY() > mHeaderQsPanel.getTop()) {
            return super.onTouchEvent(event);
        } else {
            return false;
        }
    }

    void updateResources() {
        Resources resources = mContext.getResources();
        boolean largeScreenHeaderActive =
                LargeScreenUtils.shouldUseLargeScreenShadeHeader(resources);
        
        int statusBarSideMargin = mHeaderImageEnabled ? mContext.getResources().getDimensionPixelSize(
                R.dimen.qs_header_image_side_margin) : 0;
        ViewGroup.LayoutParams lp = getLayoutParams();
        if (mQsDisabled) {
            lp.height = 0;
        } else {
            lp.height = WRAP_CONTENT;
        }
        setLayoutParams(lp);

        MarginLayoutParams qqsLP = (MarginLayoutParams) mHeaderQsPanel.getLayoutParams();
        if (largeScreenHeaderActive) {
            qqsLP.topMargin = mContext.getResources()
                    .getDimensionPixelSize(R.dimen.qqs_layout_margin_top);
        } else {
            qqsLP.topMargin = mContext.getResources()
                    .getDimensionPixelSize(R.dimen.large_screen_shade_header_min_height);
        }
        mHeaderQsPanel.setLayoutParams(qqsLP);
        
        updateAnciHeaderimgSet();
    }
    
    private void updateAnciHeaderimgSet() {

        Resources resources = mContext.getResources();

        int orientation = getResources().getConfiguration().orientation; 
	mColorAccent = Utils.getColorAttrDefaultColor(mContext, android.R.attr.colorAccent);
        mColorTextPrimary = Utils.getColorAttrDefaultColor(mContext, android.R.attr.textColorPrimary);
	mColorWindow = Utils.getColorAttrDefaultColor(mContext, android.R.attr.windowBackground);
        mColorPutihIreng = mContext.getResources().getColor(R.color.puteh_ireng);

	int fkheightzero = resources.getDimensionPixelSize(R.dimen.ancient_qs_zero);  
	int headersmall = resources.getDimensionPixelSize(R.dimen.ancient_header_small);   
	int headerbig = resources.getDimensionPixelSize(R.dimen.ancient_header_big);

	if (mHeaderImageEnabled) {
	     if (mHeaderImageLandDisabled && orientation == Configuration.ORIENTATION_LANDSCAPE) {
                mBackgroundImage.setVisibility(View.GONE);
             } else {
                mBackgroundImage.setVisibility(View.VISIBLE);
             mBackgroundImage.setVisibility(View.VISIBLE);
             }

	     if (mAncientUIheaderImgStyle == 0) {
		 mBackgroundImage.setImageResource(R.drawable.anime1);
             } else if (mAncientUIheaderImgStyle == 1) {
		 mBackgroundImage.setImageResource(R.drawable.anime2);
             } else if (mAncientUIheaderImgStyle == 2) {
		 mBackgroundImage.setImageResource(R.drawable.anime3);
             } else if (mAncientUIheaderImgStyle == 3) {
		 mBackgroundImage.setImageResource(R.drawable.anime4);
             } else if (mAncientUIheaderImgStyle == 4) {
		 mBackgroundImage.setImageResource(R.drawable.anime5);
             } else if (mAncientUIheaderImgStyle == 5) {
		 mBackgroundImage.setImageResource(R.drawable.anime6);
             } else if (mAncientUIheaderImgStyle == 6) {
		 mBackgroundImage.setImageResource(R.drawable.anime7);
             } else if (mAncientUIheaderImgStyle == 7) {
		 mBackgroundImage.setImageResource(R.drawable.anime8);
             } else if (mAncientUIheaderImgStyle == 8) {
		 mBackgroundImage.setImageResource(R.drawable.anime9);
             } else if (mAncientUIheaderImgStyle == 9) {
		 mBackgroundImage.setImageResource(R.drawable.anime10);
             } else if (mAncientUIheaderImgStyle == 10) {
		 mBackgroundImage.setImageResource(R.drawable.anime11);
             } else if (mAncientUIheaderImgStyle == 11) {
		 mBackgroundImage.setImageResource(R.drawable.anime12);
             } else if (mAncientUIheaderImgStyle == 12) {
		 mBackgroundImage.setImageResource(R.drawable.anime13);
             } else if (mAncientUIheaderImgStyle == 13) {
		 mBackgroundImage.setImageResource(R.drawable.anime14);
	     } else if (mAncientUIheaderImgStyle == 14) {
		 mBackgroundImage.setImageResource(R.drawable.anime15);
	     } else if (mAncientUIheaderImgStyle == 15) {
		 mBackgroundImage.setImageResource(R.drawable.banner1);
	     } else if (mAncientUIheaderImgStyle == 16) {
		 mBackgroundImage.setImageResource(R.drawable.banner2);
	     } else if (mAncientUIheaderImgStyle == 17) {
		 mBackgroundImage.setImageResource(R.drawable.flower1);
	     } else if (mAncientUIheaderImgStyle == 18) {
	 	 mBackgroundImage.setImageResource(R.drawable.flower2);
	     } else if (mAncientUIheaderImgStyle == 19) {
		 mBackgroundImage.setImageResource(R.drawable.planetary1);
             } else if (mAncientUIheaderImgStyle == 20) {
		 mBackgroundImage.setImageResource(R.drawable.planetary2);
	     } else if (mAncientUIheaderImgStyle == 21) {
		 mBackgroundImage.setImageResource(R.drawable.scene1);
	     } else if (mAncientUIheaderImgStyle == 22) {
		 mBackgroundImage.setImageResource(R.drawable.scene2);
	     } else if (mAncientUIheaderImgStyle == 23) {
	 	 mBackgroundImage.setImageResource(R.drawable.scene3);
	     } else if (mAncientUIheaderImgStyle == 24) {
		 mBackgroundImage.setImageResource(R.drawable.white1);
	     }

             if (digawefull) {
                mBackgroundImage.setScaleType(ImageView.ScaleType.FIT_XY);
	     } else {
                mBackgroundImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
	     }

             mBackgroundImage.setAlpha(mAncientUIheaderAlphaStyle);

             if (mAncientUIheaderTintStyle == 0) {
		 mBackgroundImage.setColorFilter(null);
	     } else if (mAncientUIheaderTintStyle == 1) {
		 mBackgroundImage.setColorFilter(mColorAccent);
	     } else if (mAncientUIheaderTintStyle == 2) {
		 mBackgroundImage.setColorFilter(mColorTextPrimary);
	     } else if (mAncientUIheaderTintStyle == 3) {
		 mBackgroundImage.setColorFilter(mColorWindow);
	     } else if (mAncientUIheaderTintStyle == 4) {
		 mBackgroundImage.setColorFilter(mColorPutihIreng);
	     } else if (mAncientUIheaderTintStyle == 5) {
		 mBackgroundImage.setColorFilter(EvlUtils.getRandomColor(mContext));
	     } else if (mAncientUIheaderTintStyle == 6) {
		 mBackgroundImage.setColorFilter(mAncientUIheaderTintStyleCustom);
	     }

        } else { 

	     mBackgroundImage.setVisibility(View.GONE);	

	}

	ViewGroup.MarginLayoutParams jembut = (ViewGroup.MarginLayoutParams) mStatusBarHeaderMachineLayout.getLayoutParams();
	          if (mHeaderImageEnabled) {    
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {  
                if (mHeaderImageHeightEnabled) {  
                   jembut.height = jembutpanjangheight;
                } else {  
                   jembut.height = headersmall;
                }
            } else {    
                if (mHeaderImageHeightEnabled) {  
                   jembut.height = jembutheight;
                } else {  
                     jembut.height = headersmall;
                }
            }
          } else {  
              jembut.height = fkheightzero;
          }
	jembut.setMargins(jembutpinggir, jembutduwur, jembutpinggir, 0);
	mStatusBarHeaderMachineLayout.setLayoutParams(jembut); 

    }

    public void setExpanded(boolean expanded, QuickQSPanelController quickQSPanelController) {
        if (mExpanded == expanded) return;
        mExpanded = expanded;
        quickQSPanelController.setExpanded(expanded);
    }

    public void disable(int state1, int state2, boolean animate) {
        final boolean disabled = (state2 & DISABLE2_QUICK_SETTINGS) != 0;
        if (disabled == mQsDisabled) return;
        mQsDisabled = disabled;
        mHeaderQsPanel.setDisabledByPolicy(disabled);
        updateResources();
    }

    private void setContentMargins(View view, int marginStart, int marginEnd) {
        MarginLayoutParams lp = (MarginLayoutParams) view.getLayoutParams();
        lp.setMarginStart(marginStart);
        lp.setMarginEnd(marginEnd);
        view.setLayoutParams(lp);
    }

    @Override
    public void onTuningChanged(String key, String newValue) {
        if (ANCIENT_UI_HEADERIMG_STYLE.equals(key)) {
            mAncientUIheaderStyle = TunerService.parseInteger(newValue, 0);
            updateResources();
	} else if (ANCIENT_UI_HEADERIMG_ANIMATION.equals(key)) {
            mAncientUIheaderAniStyle = TunerService.parseInteger(newValue, 0);
            updateResources();	
	} else if (ANCIENT_UI_HEADERIMG_SET.equals(key)) {
            mAncientUIheaderImgStyle = TunerService.parseInteger(newValue, 0);
            updateResources();	
	} else if (ANCIENT_UI_HEADERIMG_SWITCH.equals(key)) {
            mHeaderImageEnabled = TunerService.parseIntegerSwitch(newValue, false);
            updateResources();
        } else if (ANCIENT_UI_HEADERIMG_LAND_SWITCH.equals(key)) {
            mHeaderImageLandDisabled = TunerService.parseIntegerSwitch(newValue, true);
            updateResources();	
	} else if (ANCIENT_UI_HEADERIMG_TINT.equals(key)) {
            mAncientUIheaderTintStyle = TunerService.parseInteger(newValue, 0);
            updateResources();	
	} else if (ANCIENT_UI_HEADERIMG_TINT_CUSTOM.equals(key)) {
            mAncientUIheaderTintStyleCustom = TunerService.parseInteger(newValue, 0XFFFFFFFF);
            updateResources();		
	} else if (ANCIENT_UI_HEADERIMG_ALPHA.equals(key)) {
            mAncientUIheaderAlphaStyle = TunerService.parseInteger(newValue, 255);
            updateResources();			
	} else if (ANCIENT_UI_HEADERIMG_USECUSTOMHEIGHT.equals(key)) {
            mHeaderImageHeightEnabled = TunerService.parseIntegerSwitch(newValue, false);
            updateResources();			
	} else if (IMAGE_HEADER_HEIGHTP.equals(key)) {
            jembutheight = TunerService.parseInteger(newValue, 155);
            updateResources();			
	} else if (IMAGE_HEADER_HEIGHTL.equals(key)) {
            jembutpanjangheight = TunerService.parseInteger(newValue, 155);
            updateResources();			
	} else if (IMAGE_HEADER_HEIGHTPING.equals(key)) {
            jembutpinggir = TunerService.parseInteger(newValue, 0);
            updateResources();			
	} else if (IMAGE_HEADER_HEIGHTNDU.equals(key)) {
            jembutduwur = TunerService.parseInteger(newValue, 0);
            updateResources();
        } else if (IMAGE_HEADER_SCALETYPE.equals(key)) {
            digawefull = TunerService.parseIntegerSwitch(newValue, false);
            updateResources();      
        }
    }
}
