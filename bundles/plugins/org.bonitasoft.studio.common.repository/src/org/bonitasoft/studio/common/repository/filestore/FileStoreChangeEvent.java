/**
 * Copyright (C) 2012 BonitaSoft S.A.
 * BonitaSoft, 31 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.bonitasoft.studio.common.repository.filestore;

import java.util.Map;

import org.bonitasoft.studio.common.repository.model.IRepositoryFileStore;

/**
 * @author Romain Bioteau
 *
 */
public class FileStoreChangeEvent {

    public enum EventType {PRE_OPEN,POST_OPEN,POST_CLOSE,PRE_CLOSE,PRE_DELETE,POST_DELETE,PRE_SAVE,POST_SAVE,POST_IMPORT, PRE_IMPORT}

    private final EventType event;
    private final IRepositoryFileStore fileStore;
    Map<String, Object> parameters;

    public FileStoreChangeEvent(final EventType event,final IRepositoryFileStore fileStore){
        this.event = event ;
        this.fileStore = fileStore ;
    }

    public FileStoreChangeEvent(final EventType event, final IRepositoryFileStore fileStore, final Map<String, Object> parameters) {
        this(event, fileStore);
        this.parameters = parameters;
    }

    public EventType getEvent() {
        return event;
    }

    public IRepositoryFileStore getFileStore() {
        return fileStore;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public Object getParameter(final String key) {
        if (parameters != null) {
            return parameters.get(key);
        }
        return null;
    }

}
