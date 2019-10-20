package de.leonhard.storage.internal.base;

import de.leonhard.storage.internal.base.exceptions.InvalidSettingException;
import de.leonhard.storage.internal.enums.FileType;
import de.leonhard.storage.internal.enums.ReloadSetting;
import de.leonhard.storage.internal.utils.FileUtils;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;


@SuppressWarnings({"UnusedReturnValue", "unused", "WeakerAccess"})
@Getter
public abstract class FlatFile implements StorageBase, Comparable<FlatFile> {

	protected File file;
	protected FileData fileData = new FileData();
	protected FileType fileType;

	@Setter
	private String pathPrefix;
	@Setter
	private ReloadSetting reloadSetting = ReloadSetting.INTELLIGENT;
	private long lastModified;


	public final String getFilePath() {
		return this.file.getAbsolutePath();
	}

	public final String getName() {
		return this.file.getName();
	}


	@Override
	public boolean hasKey(final String key) {
		String tempKey = (pathPrefix == null) ? key : pathPrefix + "." + key;
		update();
		return fileData.containsKey(tempKey);
	}

	/**
	 * Checks if the File needs to be reloaded and does so if true.
	 */
	protected final void update() {
		try {
			if (shouldReload()) {
				reload();
			}
		} catch (InvalidSettingException e) {
			e.printStackTrace();
		}
	}

	protected boolean shouldReload() throws InvalidSettingException {
		if (reloadSetting.equals(ReloadSetting.AUTOMATICALLY)) {
			return true;
		} else if (reloadSetting.equals(ReloadSetting.INTELLIGENT)) {
			return hasChanged();
		} else if (reloadSetting.equals(ReloadSetting.MANUALLY)) {
			return false;
		} else {
			throw new InvalidSettingException("No proper ReloadSetting");
		}
	}

	/**
	 * Returns if the File has changed since the last check.
	 *
	 * @return true if it has changed.
	 */
	public final boolean hasChanged() {
		return FileUtils.hasChanged(file, lastModified);
	}

	/**
	 * Reread the content of our flat file
	 */
	public abstract void reload();

	@Override
	public Set<String> keySet() {
		update();
		return fileData.keySet();
	}

	@Override
	public Set<String> keySet(final String key) {
		update();
		return fileData.keySet(key);
	}

	@Override
	public Set<String> singleLayerKeySet() {
		update();
		return fileData.singleLayerKeySet();
	}

	@Override
	public Set<String> singleLayerKeySet(final String key) {
		update();
		return fileData.singleLayerKeySet(key);
	}

	public void replace(final CharSequence target, final CharSequence replacement) throws IOException {
		final List<String> lines = Files.readAllLines(file.toPath());
		final List<String> result = new ArrayList<>();
		for (String line : lines) {
			result.add(line.replace(target, replacement));
		}
		Files.write(file.toPath(), result);
	}

	/**
	 * Creates an empty file.
	 *
	 * @param file the file to be created.
	 * @return true if file was created.
	 */
	protected final synchronized boolean create(final File file) {
		this.fileType = FileTypeUtils.getFileType(file);
		this.file = file;
		if (file.exists()) {
			lastModified = System.currentTimeMillis();
			return false;
		} else {
			FileUtils.createFile(file);
			lastModified = System.currentTimeMillis();
			return true;
		}
	}

	protected final FlatFile getFlatFileInstance() {
		return this;
	}

	@Override
	public synchronized int hashCode() {
		return this.file.hashCode();
	}

	@Override
	public synchronized int compareTo(final FlatFile flatFile) {
		return this.file.compareTo(flatFile.file);
	}

	@Override
	public synchronized String toString() {
		return this.file.getAbsolutePath();
	}

	@Override
	public synchronized boolean equals(final Object obj) {
		if (obj == this) {
			return true;
		} else if (obj == null || this.getClass() != obj.getClass()) {
			return false;
		} else {
			FlatFile flatFile = (FlatFile) obj;
			return this.pathPrefix.equals(flatFile.pathPrefix)
				   && reloadSetting.equals(flatFile.reloadSetting)
				   && this.fileData.equals(flatFile.fileData)
				   && this.file.equals(flatFile.file)
				   && fileType.equals(flatFile.fileType)
				   && this.lastModified == flatFile.lastModified;
		}
	}
}