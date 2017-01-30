package org.bonitasoft.studio.importer.bos.model;

import static java.util.Objects.requireNonNull;

import java.util.Optional;

import org.bonitasoft.studio.common.repository.model.IRepositoryFileStore;
import org.bonitasoft.studio.common.repository.model.IRepositoryStore;

public abstract class AbstractImportModel {

    protected ConflictStatus status = ConflictStatus.NONE;
    protected Optional<AbstractFolderModel> parent;
    protected String path;
    protected Optional<String> displayName = Optional.empty();

    public AbstractImportModel(String path, AbstractFolderModel parent) {
        requireNonNull(path);
        this.path = path;
        this.parent = Optional.ofNullable(parent);
    }

    public Optional<AbstractFolderModel> getParent() {
        return parent;
    }

    public String getPath() {
        return path;
    }

    public void setDisplayName(String displayName) {
        this.displayName = Optional.ofNullable(displayName);
    }

    public ConflictStatus getStatus() {
        return status;
    }

    public void setStatus(ConflictStatus status) {
        this.status = status;
        if (parent.isPresent() && isConflicting() && !parent.get().isConflicting()) {
            parent.get().setStatus(status);
        }
    }

    public boolean isConflicting() {
        return status == ConflictStatus.CONFLICTING;
    }

    protected Optional<IRepositoryStore<IRepositoryFileStore>> getParentRepositoryStore() {
        final Optional<AbstractFolderModel> folder = getParent();
        return folder.flatMap(this::getParentStoreModel);
    }

    private Optional<IRepositoryStore<IRepositoryFileStore>> getParentStoreModel(AbstractFolderModel folder) {
        Optional<AbstractFolderModel> current = Optional.ofNullable(folder);
        while (current.isPresent() && !(current.get() instanceof ImportStoreModel)) {
            current = current.flatMap(AbstractImportModel::getParent);
        }
        return current.map(ImportStoreModel.class::cast)
                .flatMap(store -> Optional.ofNullable(store.getRepositoryStore()));
    }

}