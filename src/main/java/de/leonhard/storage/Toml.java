package de.leonhard.storage;

import de.leonhard.storage.internal.base.FileTypeUtils;
import de.leonhard.storage.internal.base.exceptions.InvalidFileTypeException;
import de.leonhard.storage.internal.datafiles.raw.TomlFile;
import de.leonhard.storage.internal.enums.FileType;
import de.leonhard.storage.internal.enums.ReloadSetting;
import java.io.File;
import java.io.InputStream;


@SuppressWarnings("unused")
public class Toml extends TomlFile {

	public Toml(final File file) throws InvalidFileTypeException {
		super(file, null, null);
	}

	public Toml(final String name, final String path) throws InvalidFileTypeException {
		super(new File(path, FileTypeUtils.addExtension(name, FileType.TOML)), null, null);
	}

	public Toml(final String name, final String path, final ReloadSetting reloadSetting) throws InvalidFileTypeException {
		super(new File(path, FileTypeUtils.addExtension(name, FileType.TOML)), null, reloadSetting);
	}

	public Toml(final File file, final InputStream inputStream, final ReloadSetting reloadSetting) throws InvalidFileTypeException {
		super(file, inputStream, reloadSetting);
	}

	public boolean contains(final String key) {
		return hasKey(key);
	}
}