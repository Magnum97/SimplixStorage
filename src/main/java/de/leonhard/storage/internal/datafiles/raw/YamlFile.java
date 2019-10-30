package de.leonhard.storage.internal.datafiles.raw;

import com.esotericsoftware.yamlbeans.YamlException;
import com.esotericsoftware.yamlbeans.YamlReader;
import com.esotericsoftware.yamlbeans.YamlWriter;
import de.leonhard.storage.internal.base.CommentEnabledFile;
import de.leonhard.storage.internal.base.FileData;
import de.leonhard.storage.internal.datafiles.section.YamlSection;
import de.leonhard.storage.internal.editor.YamlEditor;
import de.leonhard.storage.internal.enums.Comment;
import de.leonhard.storage.internal.enums.DataType;
import de.leonhard.storage.internal.enums.Reload;
import de.leonhard.storage.internal.utils.FileUtils;
import de.leonhard.storage.internal.utils.YamlUtils;
import de.leonhard.storage.internal.utils.basic.Valid;
import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.Cleanup;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


/**
 * Class to manager Yaml-Type Files
 */
@SuppressWarnings({"unchecked", "unused"})
public class YamlFile extends CommentEnabledFile {

	protected final YamlEditor yamlEditor;
	private final YamlUtils parser;

	public YamlFile(@NotNull final File file, @Nullable final InputStream inputStream, @Nullable final Reload reloadSetting, @Nullable final Comment commentSetting, @Nullable final DataType dataType) {
		super(file, FileType.YAML);
		if (create() && inputStream != null) {
			FileUtils.writeToFile(this.file, inputStream);
		}

		if (commentSetting != null) {
			setCommentSetting(commentSetting);
		}
		if (dataType != null) {
			setDataType(dataType);
		} else {
			setDataType(DataType.STANDARD);
		}
		if (reloadSetting != null) {
			setReloadSetting(reloadSetting);
		}

		this.yamlEditor = new YamlEditor(this.file);
		this.parser = new YamlUtils(yamlEditor);

		try {
			this.fileData = new FileData((Map<String, Object>) new YamlReader(new FileReader(this.file)).read());
			this.lastLoaded = System.currentTimeMillis();
		} catch (YamlException | FileNotFoundException e) {
			System.err.println("Exception while reloading '" + this.file.getAbsolutePath() + "'");
			e.printStackTrace();
			throw new IllegalStateException();
		}
	}


	@Override
	public void reload() {
		try {
			fileData.loadData((Map<String, Object>) new YamlReader(new FileReader(this.file)).read());
			this.lastLoaded = System.currentTimeMillis();
		} catch (IOException e) {
			System.err.println("Exception while reloading '" + this.file.getAbsolutePath() + "'");
			e.printStackTrace();
			throw new IllegalStateException();
		}
	}

	@Override
	public Object get(@NotNull final String key) {
		Valid.notNull(key, "Key must not be null");
		update();
		return fileData.get(key);
	}

	@Override
	public synchronized void remove(@NotNull final String key) {
		Valid.notNull(key, "Key must not be null");

		update();

		if (fileData.containsKey(key)) {
			fileData.remove(key);

			try {
				write(fileData.toMap());
			} catch (IOException e) {
				System.err.println("Could not write to '" + this.file.getAbsolutePath() + "'");
				e.printStackTrace();
				throw new IllegalStateException();
			}
		}
	}

	private void write(@NotNull final Map fileData) throws IOException {
		@Cleanup YamlWriter writer = new YamlWriter(new FileWriter(this.file));
		writer.write(fileData);
	}

	@Override
	public void set(@NotNull final String key, @Nullable final Object value) {
		set(key, value, this.getCommentSetting());
	}

	public synchronized void set(@NotNull final String key, @Nullable final Object value, @NotNull final Comment commentSetting) {
		if (insert(key, value)) {
			try {
				if (!Comment.PRESERVE.equals(commentSetting)) {
					write(Objects.requireNonNull(fileData).toMap());
				} else {
					final List<String> unEdited = yamlEditor.read();
					final List<String> header = yamlEditor.readHeader();
					final List<String> footer = yamlEditor.readFooter();
					write(fileData.toMap());
					header.addAll(yamlEditor.read());
					if (!header.containsAll(footer)) {
						header.addAll(footer);
					}
					yamlEditor.write(parser.parseComments(unEdited, header));
					write(Objects.requireNonNull(fileData).toMap());
				}
			} catch (IOException e) {
				System.err.println("Error while writing to '" + getAbsolutePath() + "'");
				e.printStackTrace();
				throw new IllegalStateException();
			}
		}
	}

	public YamlSection getYamlSection(@NotNull final String key) {
		return new YamlSection(this, key);
	}

	protected final YamlFile getYamlFileInstance() {
		return this;
	}

	@Override
	public boolean equals(@Nullable final Object obj) {
		if (obj == this) {
			return true;
		} else if (obj == null || this.getClass() != obj.getClass()) {
			return false;
		} else {
			YamlFile yaml = (YamlFile) obj;
			return this.getCommentSetting().equals(yaml.getCommentSetting())
				   && super.equals(yaml.getFlatFileInstance());
		}
	}
}