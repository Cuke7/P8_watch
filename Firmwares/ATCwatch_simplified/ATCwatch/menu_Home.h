/*
 * Copyright (c) 2020 Aaron Christophel
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

#pragma once
#include "Arduino.h"
#include "class.h"
#include "images.h"
#include "menu.h"
#include "inputoutput.h"
#include "display.h"
#include "ble.h"
#include "time.h"
#include "battery.h"
#include "accl.h"
#include "push.h"
#include "heartrate.h"
#include "fonts.h"
#include "sleep.h"
#include <lvgl.h>

// Weather icons
#include "icon_01d.h"
#include "icon_02d.h"
#include "icon_03d.h"
#include "icon_04d.h"
#include "icon_09d.h"
#include "icon_10d.h"
#include "icon_11d.h"
#include "icon_13d.h"
#include "icon_50d.h"

class HomeScreen : public Screen
{
public:
  virtual void pre()
  {
    time_data = get_time();
    accl_data = get_accl_data();

    lv_style_copy(&st, &lv_style_plain);
    st.text.color = lv_color_hsv_to_rgb(10, 5, 95);
    st.text.font = &mksd50;

    label_time = lv_label_create(lv_scr_act(), NULL);
    lv_label_set_text_fmt(label_time, "%02i:%02i", time_data.hr, time_data.min);
    lv_obj_set_style(label_time, &st);
    lv_obj_align(label_time, NULL, LV_ALIGN_CENTER, 0, -10);

    // BLE ET BATTERY
    label_battery = lv_label_create(lv_scr_act(), NULL);
    lv_obj_align(label_battery, lv_scr_act(), LV_ALIGN_IN_TOP_LEFT, 25, 5);
    lv_label_set_text_fmt(label_battery, "%i%%", get_battery_percent());

    label_ble = lv_label_create(lv_scr_act(), NULL);
    lv_obj_align(label_ble, lv_scr_act(), LV_ALIGN_IN_TOP_LEFT, 5, 5);
    lv_label_set_text(label_ble, LV_SYMBOL_BLUETOOTH);

    lv_style_copy(&style_ble, lv_label_get_style(label_ble, LV_LABEL_STYLE_MAIN));
    style_ble.text.color = LV_COLOR_RED;
    style_ble.text.font = LV_FONT_DEFAULT;
    lv_obj_set_style(label_ble, &style_ble);

    lv_style_copy(&style_battery, lv_label_get_style(label_battery, LV_LABEL_STYLE_MAIN));
    style_battery.text.color = lv_color_hsv_to_rgb(10, 5, 95);
    lv_obj_set_style(label_battery, &style_battery);

    // DATE
    label_date = lv_label_create(lv_scr_act(), NULL);

    lv_style_copy(&style_msg, lv_label_get_style(label_ble, LV_LABEL_STYLE_MAIN));
    style_msg.text.font = &sans_regular;
    style_msg.text.color = lv_color_hsv_to_rgb(10, 5, 95);
    //style_msg.text.font = &sans_bold28;
    lv_obj_set_style(label_date, &style_msg);
    lv_label_set_text_fmt(label_date, "%s %02i/%02i", string2char(zellersAlgorithm(time_data.day, time_data.month, time_data.year)), time_data.day, time_data.month);
    lv_obj_align(label_date, NULL, LV_ALIGN_CENTER, 0, 30);

    // METEO

    String meteo = get_meteo();
    String icons[7] = {"Saturday", "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday"};
    int commaIndex = meteo.indexOf(";");
    String icon = meteo.substring(commaIndex + 1, meteo.length());

    img_msg = lv_img_create(lv_scr_act(), NULL);

    if (icon == "01d" || icon == "01n")
    {
      lv_img_set_src(img_msg, &I01d_icon);
    }
    else if (icon == "02d" || icon == "02n")
    {
      lv_img_set_src(img_msg, &I02d_icon);
    }
    else if (icon == "03d" || icon == "03n")
    {
      lv_img_set_src(img_msg, &I03d_icon);
    }
    else if (icon == "04d" || icon == "04n")
    {
      lv_img_set_src(img_msg, &I04d_icon);
    }
    else if (icon == "09d" || icon == "09n")
    {
      lv_img_set_src(img_msg, &I09d_icon);
      show_push("Pluie probable\n ");
    }
    else if (icon == "10d" || icon == "10n")
    {
      lv_img_set_src(img_msg, &I10d_icon);
      show_push("Pluie probable\n ");
    }
    else if (icon == "11d" || icon == "11n")
    {
      lv_img_set_src(img_msg, &I11d_icon);
      show_push("Pluie probable\n ");
    }
    else if (icon == "13d" || icon == "13n")
    {
      lv_img_set_src(img_msg, &I13d_icon);
    }
    else if (icon == "50d" || icon == "50n")
    {
      lv_img_set_src(img_msg, &I50d_icon);
    }
    else
    {
      lv_img_set_src(img_msg, &I01d_icon);
    }

    lv_obj_align(img_msg, NULL, LV_ALIGN_IN_BOTTOM_LEFT, 0, 5);
    label_msg = lv_label_create(lv_scr_act(), NULL);

    lv_style_copy(&style_msg, lv_label_get_style(label_ble, LV_LABEL_STYLE_MAIN));
    style_msg.text.color = lv_color_hsv_to_rgb(10, 5, 95);
    style_msg.text.font = &sans_regular;
    lv_obj_set_style(label_msg, &style_msg);

    lv_obj_set_width(label_msg, 240);
    lv_label_set_text(label_msg, " ");
    lv_label_set_text(label_msg, string2char(get_meteo().substring(0, commaIndex)));
    lv_obj_align(label_msg, img_msg, LV_ALIGN_OUT_RIGHT_MID, 2, 0);

    // HEART RATE

    /*img_heart = lv_img_create(lv_scr_act(), NULL);
    lv_img_set_src(img_heart, &Iheart_rate);
    lv_obj_align(img_heart, img_msg, LV_ALIGN_OUT_TOP_MID, 0, 10);

    label_heart = lv_label_create(lv_scr_act(), NULL);
    lv_obj_set_style(label_heart, &style_msg);
    lv_obj_set_width(label_heart, 240);
    lv_label_set_text_fmt(label_heart, "%i", get_last_heartrate());
    lv_obj_align(label_heart, img_heart, LV_ALIGN_OUT_RIGHT_MID, 2, 0);*/

    // Arc seconds
    lv_style_copy(&arc_style, &lv_style_plain);
    arc_style.line.color = LV_COLOR_BLUE;
    arc_style.line.width = 5;
    arc_style.line.rounded = true;

    arc = lv_arc_create(lv_scr_act(), NULL);
    lv_obj_set_size(arc, 165, 165);
    lv_arc_set_style(arc, LV_ARC_STYLE_MAIN, &arc_style);
    lv_obj_align(arc, NULL, LV_ALIGN_CENTER, 0, 0);

    /*
    // Arc minutes
    lv_style_copy(&arc_style2, &lv_style_plain);
    arc_style2.line.color = LV_COLOR_BLUE;
    arc_style2.line.width = 10;
    arc_style2.line.rounded = false;

    arc2 = lv_arc_create(lv_scr_act(), NULL);
    lv_obj_set_size(arc2, 175, 175);
    lv_arc_set_style(arc2, LV_ARC_STYLE_MAIN, &arc_style2);
    lv_obj_align(arc2, NULL, LV_ALIGN_CENTER, 0, 0);
    
     // Arc hours
    lv_style_copy(&arc_style3, &lv_style_plain);
    arc_style3.line.color = LV_COLOR_BLUE;
    arc_style3.line.width = 10;
    arc_style3.line.rounded = false;

    arc3 = lv_arc_create(lv_scr_act(), NULL);
    lv_obj_set_size(arc3, 185, 185);
    lv_arc_set_style(arc3, LV_ARC_STYLE_MAIN, &arc_style3);
    lv_obj_align(arc3, NULL, LV_ALIGN_CENTER, 0, 0);
    */
    
  }

  virtual void main()
  {
    time_data = get_time();
    accl_data = get_accl_data();

    lv_label_set_text_fmt(label_time, "%02i:%02i", time_data.hr, time_data.min);
    lv_label_set_text_fmt(label_date, "%s %02i/%02i", string2char(zellersAlgorithm(time_data.day, time_data.month, time_data.year)), time_data.day, time_data.month);
    //lv_label_set_text_fmt(label_heart, "%i", get_last_heartrate());

    lv_label_set_text_fmt(label_battery, "%i%%", get_battery_percent());

    if (get_vars_ble_connected())
      style_ble.text.color = LV_COLOR_MAKE(0x27, 0xA6, 0xFF);
    else
      style_ble.text.color = LV_COLOR_RED;
    lv_obj_set_style(label_ble, &style_ble);

    if (get_charge())
      style_battery.text.color = lv_color_hsv_to_rgb(10, 5, 95);
    else
      style_battery.text.color = LV_COLOR_MAKE(0x05, 0xF9, 0x25);
    lv_obj_set_style(label_battery, &style_battery);

    arc_style.line.color = lv_color_hsv_to_rgb(time_data.sec * 6 , 100, 100);
    if(time_data.sec < 30){
      lv_arc_set_angles(arc, 180 - (time_data.sec * 360 / 60), 180);
    }else{
      lv_arc_set_angles(arc, 540 - (time_data.sec * 360 / 60), 180);
    }

    /*arc_style2.line.color = lv_color_hsv_to_rgb(time_data.min * 6 , 100, 100);
    if(time_data.min < 30){
      lv_arc_set_angles(arc2, 180 - (time_data.min * 360 / 60), 180);
    }else{
      lv_arc_set_angles(arc2, 540 - (time_data.min * 360 / 60), 180);
    }

    arc_style3.line.color = lv_color_hsv_to_rgb(time_data.hr * 360/24 , 100, 100);
    if(time_data.hr < 30){
      lv_arc_set_angles(arc3, 180 - (time_data.hr * 360/24), 180);
    }else{
      lv_arc_set_angles(arc3, 540 - (time_data.hr * 360/24), 180);
    }*/
  }

  virtual void up()
  {
    inc_vars_menu();
  }

  virtual void down()
  {
    dec_vars_menu();
  }

  virtual void left()
  {
  }

  virtual void right()
  {
  }

  virtual void button_push(int length)
  {
    sleep_down();
  }

private:
  time_data_struct time_data;
  accl_data_struct accl_data;
  lv_style_t st, arc_style, arc_style2, arc_style3;
  lv_obj_t *label, *label_heart, *label_steps, *label_msg, *arc, *arc2, *arc3;
  lv_obj_t *label_time, *label_date, *label_meteo;
  lv_obj_t *label_ble, *label_battery;
  lv_style_t style_ble, style_battery, style_msg;
  lv_obj_t *img_heart, *img_steps, *img_msg;
  int r, g, b;

  char *string2char(String command)
  {
    if (command.length() != 0)
    {
      char *p = const_cast<char *>(command.c_str());
      return p;
    }
  }

  String zellersAlgorithm(int day, int month, int year)
  {
    int mon;
    String weekday[7] = {"Sat", "Sun", "Mon", "Tue", "Wed", "Thu", "Fri"};
    if (month > 2)
      mon = month; //for march to december month code is same as month
    else
    {
      mon = (12 + month); //for Jan and Feb, month code will be 13 and 14 year--; //decrease year for month Jan and Feb
    }
    int y = year % 100; //last two digit
    int c = year / 100; //first two digit
    int w = (day + floor((13 * (mon + 1)) / 5) + y + floor(y / 4) + floor(c / 4) + (5 * c));
    w = w % 7;
    return weekday[w];
  }
};

HomeScreen homeScreen;
