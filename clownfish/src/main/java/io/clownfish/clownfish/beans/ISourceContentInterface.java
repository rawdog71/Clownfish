/*
 * Copyright 2021 raine.
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
package io.clownfish.clownfish.beans;

import javax.faces.event.ActionEvent;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.event.ValueChangeEvent;
import org.primefaces.event.SlideEndEvent;

/**
 *
 * @author raine
 */
public interface ISourceContentInterface {
    public void init();
    public String getContent();
    public void setContent(String content);
    public void refresh();
    public void onSelect(AjaxBehaviorEvent event);
    public void onSave(ActionEvent actionEvent);
    public void onCommit(ActionEvent actionEvent);
    public void onCheckOut(ActionEvent actionEvent);
    public void onCheckIn(ActionEvent actionEvent);
    public void onChangeName(ValueChangeEvent changeEvent);
    public void onCreate(ActionEvent actionEvent);
    public void onDelete(ActionEvent actionEvent);
    public void onChange(ActionEvent actionEvent);
    public void onVersionSelect(ActionEvent actionEvent);
    public void onSlideEnd(SlideEndEvent event);
    public void onVersionChanged();
    public void writeVersion(long contentref, long version, byte[] content);
}
