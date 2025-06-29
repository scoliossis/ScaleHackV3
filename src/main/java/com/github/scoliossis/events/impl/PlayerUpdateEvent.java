package com.github.scoliossis.events.impl;

import com.github.scoliossis.events.Event;
import com.github.scoliossis.utils.RotationUtil;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class PlayerUpdateEvent extends Event {
    public RotationUtil.Rotation rotation;
}
