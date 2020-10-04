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
#include "display.h"
#include "ble.h"
#include "time.h"
#include "battery.h"
#include "accl.h"
#include "push.h"
#include "heartrate.h"
#include "fonts.h"


class NotifyScreen : public Screen
{
  public:
    virtual void pre()
    {
      set_gray_screen_style(&sans_regular);

      label_msg = lv_label_create(lv_scr_act(), NULL);
      lv_label_set_long_mode(label_msg, LV_LABEL_LONG_BREAK);
      lv_obj_set_width(label_msg,240);
      //lv_obj_align(label_msg, NULL, LV_ALIGN_CENTER, 0, 20);

      lv_style_copy(&style_station, &lv_style_plain);
      style_station.text.color = lv_color_hsv_to_rgb(10, 5, 95);
      style_station.text.font = &sans_bold28;

      label_station = lv_label_create(lv_scr_act(), NULL);
      lv_obj_set_style(label_station, &style_station );
      lv_obj_align(label_station, NULL, LV_ALIGN_CENTER, 0, -40);

      String push_msg = get_push_msg();
      int IndexofNewLine = push_msg.indexOf('\n');
      String station = push_msg.substring(0,IndexofNewLine);
      String message = push_msg.substring(IndexofNewLine + 1, push_msg.length());
      
      lv_label_set_text_fmt(label_msg,"%s" ,string2char(message));
      lv_label_set_text_fmt(label_station,"%s" ,string2char(station));
      lv_obj_align(label_station, NULL, LV_ALIGN_CENTER, 0, -40);
      lv_obj_align(label_msg, NULL, LV_ALIGN_CENTER, 0, 20);

    }

    virtual void main()
    {
      String push_msg = get_push_msg();
      int IndexofNewLine = push_msg.indexOf('\n');
      String station = push_msg.substring(0,IndexofNewLine);
      String message = push_msg.substring(IndexofNewLine + 1, push_msg.length());
      
      lv_label_set_text_fmt(label_msg,"%s" ,string2char(message));
      lv_label_set_text_fmt(label_station,"%s" ,string2char(station));
      lv_obj_align(label_station, NULL, LV_ALIGN_CENTER, 0, -40);
      lv_obj_align(label_msg, NULL, LV_ALIGN_CENTER, 0, 20);
    }

    virtual void long_click()
    {
      display_home();
    }

    virtual void left()
    {
      display_home();
    }

    virtual void right()
    {
      display_home();
    }

    virtual void up()
    {
      display_home();
    }
    virtual void down()
    {
      display_home();
    }

    virtual void click(touch_data_struct touch_data)
    {
      display_home();
    }

  private:
    lv_obj_t *label, *label_msg, *label_station;
    lv_style_t style_station;

    char* string2char(String command) {
      if (command.length() != 0) {
        char *p = const_cast<char*>(command.c_str());
        return p;
      }
    }
};

NotifyScreen notifyScreen;
