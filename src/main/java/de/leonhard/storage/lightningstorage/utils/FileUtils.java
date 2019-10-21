package de.leonhard.storage.lightningstorage.utils;

import java.io.*;
import java.nio.file.Files;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;


@SuppressWarnings("unused")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FileUtils {

	@SuppressWarnings("ResultOfMethodCallIgnored")
	public static void createFile(@NotNull final File file) {
		try {
			if (file.getParentFile() != null && !file.getParentFile().exists()) {
				file.getParentFile().mkdirs();
			}
			if (!file.exists()) {
				file.createNewFile();
			}
		} catch (IOException e) {
			System.err.println("Error while creating file '" + file.getName() + "'.");
			System.err.println("Path: '" + file.getAbsolutePath() + "'");
			e.printStackTrace();
			throw new IllegalStateException();
		}
	}

	public static BufferedInputStream createNewInputStream(@NotNull final File file) {
		try {
			return new BufferedInputStream(new FileInputStream(file));
		} catch (IOException e) {
			System.err.println("Exception while creating InputStream from '" + file.getName() + "'");
			System.err.println("At: '" + file.getAbsolutePath() + "'");
			e.printStackTrace();
			throw new IllegalStateException("InputStream would be null");
		}
	}

	public static boolean hasChanged(@NotNull final File file, final long timeStamp) {
		return timeStamp < file.lastModified();
	}

	public static synchronized void writeToFile(@NotNull final File file, @NotNull final InputStream inputStream) {
		try (BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(file))) {
			if (!file.exists()) {
				Files.copy(inputStream, file.toPath());
			} else {
				final byte[] data = new byte[8192];
				int count;
				while ((count = inputStream.read(data, 0, 8192)) != -1) {
					outputStream.write(data, 0, count);
				}
			}
		} catch (IOException e) {
			System.err.println("Exception while copying to + '" + file.getAbsolutePath() + "'");
			e.printStackTrace();
		}
	}
}