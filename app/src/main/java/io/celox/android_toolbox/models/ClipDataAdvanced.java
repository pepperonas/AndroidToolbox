/*
 * Copyright (c) 2019 Martin Pfeffer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.celox.android_toolbox.models;

import com.pepperonas.andbasx.system.DeviceUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * @author Martin Pfeffer (celox.io)
 * @see <a href="mailto:martin.pfeffer@celox.io">martin.pfeffer@celox.io</a>
 */
public class ClipDataAdvanced {

    public enum Type {
        DEFAULT
    }

    private long timestamp;

    private Type type;
    private String content;
    private SizedText sizedText;
    private final long iv;

    public SizedText getSizedText() {
        return sizedText;
    }

    /**
     * Instantiates a new Clip data advanced.
     *
     * @param timestamp the timestamp
     * @param type      the type
     * @param content   the content
     */
    public ClipDataAdvanced(long timestamp, Type type, String content, long iv) {
        this.timestamp = timestamp;
        this.type = type;
        this.content = content;
        this.sizedText = initSizedText();
        this.iv = iv;
    }

    /**
     * Gets timestamp.
     *
     * @return the timestamp
     */
    public long getTimestamp() {
        return timestamp;
    }

    public String getCreationDate() {
        DateFormat dateFormat = new SimpleDateFormat("dd.MM", DeviceUtils.getLocale());
        String tmp = dateFormat.format(timestamp);
        return tmp.split("</>")[0];

    }

    public String getCreationTime() {
        DateFormat dateFormat = new SimpleDateFormat("HH:mm", DeviceUtils.getLocale());
        return dateFormat.format(timestamp);
    }

    /**
     * Sets timestamp.
     *
     * @param timestamp the timestamp
     */
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Gets type.
     *
     * @return the type
     */
    public Type getType() {
        return type;
    }

    /**
     * Sets type.
     *
     * @param type the type
     */
    public void setType(Type type) {
        this.type = type;
    }

    /**
     * Gets content.
     *
     * @return the content
     */
    public String getContent() {
        return content;
    }

    /**
     * Sets content.
     *
     * @param content the content
     */
    public void setContent(String content) {
        this.content = content;
    }

    private SizedText initSizedText() {
        SizedText sizedText = new SizedText();
        String tmpText = content;

        if (tmpText.contains("\n")) {
            sizedText.setTextSize(12f);
            tmpText = tmpText.replace("\n", "</>");
        }

        if (tmpText.length() > 20) {
            sizedText.setTextSize(12f);
        }

        if (tmpText.length() > 30) {
            sizedText.setTextSize(12f);
            tmpText = tmpText.substring(0, 29) + "...";
        }

        sizedText.setText(tmpText);
        return sizedText;
    }

    public long getIv() {
        return iv;
    }

    @Override
    public String toString() {
        return "ClipDataAdvanced{" +
                "timestamp=" + timestamp +
                ", type=" + type +
                ", content='" + content + '\'' +
                ", sizedText=" + sizedText +
                ", iv=" + iv +
                '}';
    }
}
