import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;

public class AppMain {
	public static AppMain application;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		application = new AppMain();
		application.internalInit();
	}

	public class FileStruct {
		public Long crcnum;
		public String url;
		public String file_name;
		public File file;
		public String parentDirectory;
	}

	public class analysiser extends Thread {
		@Override
		public void run() {
			super.run();
			for (String url : inDirectorys) {
				try {
					openDirectory(url);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		private void openDirectory(String str_path) throws IOException {
			File rootPath = new File(str_path);
			if (rootPath.exists()) {
				File[] inFiles = rootPath.listFiles();
				if (inFiles.length == 0)
					return;
				else {
					for (File node : inFiles) {
						if (!node.exists())
							return;
						if (node.isDirectory())
							openDirectory(node.getAbsolutePath());
						else {
							FileStruct fStruct = new FileStruct();
							InputStream fileStream = new FileInputStream(node.getAbsolutePath());
							CheckedInputStream cis = new CheckedInputStream(fileStream, new CRC32());
							byte[] buf = new byte[128];
							while (cis.read(buf) >= 0) {
							}

							Long crc_32 = cis.getChecksum().getValue();
							fStruct.file = node;
							fStruct.url = node.getAbsolutePath();
							fStruct.crcnum = crc_32;
							fStruct.file_name = node.getName();
							fStruct.parentDirectory = node.getParent().substring(5);
							fStruct.parentDirectory = fStruct.parentDirectory.replaceAll("\\[?\\]?\\s?", "");
							out_put_list.add(fStruct);
							crc_map.put(crc_32, fStruct);
							System.out.println(fStruct.parentDirectory);
							System.out.printf("%s====%s====%s\n",
									fStruct.file_name, fStruct.url,
									fStruct.crcnum.toString());
						}
					}
				}
			}
		}

		@Override
		public String toString() {
			return super.toString();
		}
	}

	public class outputFile extends Thread {
		public void run() {
			while (true) {
				if (out_put_list.size() > 0) {
					FileStruct it = out_put_list.get(0);
					if (output_num.indexOf(it.crcnum) > -1) {
						out_put_list.remove(it);
						continue;
					}
					output_num.add(it.crcnum);
					out_put_list.remove(0);
					try {
						File outFile = new File(out_put_url + it.parentDirectory + "\\" + it.file_name);
	
						
						if(!outFile.getParentFile().exists()) {
							//outFile.createNewFile();
							outFile.getParentFile().mkdirs();
						}
						
						if(outFile.exists()) outFile.createNewFile();
						
						BufferedInputStream inStream = new BufferedInputStream(new FileInputStream(it.url));
						BufferedOutputStream outStream = new BufferedOutputStream(new FileOutputStream(outFile));
						
					
						byte[] buf = new byte[inStream.available()];
						if (inStream.read(buf) > -1) {
							outStream.write(buf);
							System.out.println("------------------");
						}
						
						inStream.close();
						outStream.close();
						
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
		
		private void createParent(File nodeFile,Vector<String> paths) throws IOException {
			File parentFile = nodeFile.getParentFile();
			if(parentFile.isDirectory()) {
				if(!parentFile.exists()){
					paths.add(parentFile.getAbsolutePath());
					createParent(parentFile, paths);
				} else {
					while(paths.size() > 0) {
						int lastIndex = paths.size() - 1;
						String url = paths.remove(lastIndex);
						File directory = new File(url);
						if(!directory.exists()) directory.createNewFile();
					}
				}
			}
		}
		
	}

	public Properties app_properties;
	public String[] inDirectorys;
	public String out_put_url;
	public Map<Long, FileStruct> crc_map;
	public List<FileStruct> out_put_list;
	public List<Long> output_num;

	private void internalInit() {
		crc_map = new HashMap<Long, FileStruct>();
		app_properties = new Properties();
		out_put_list = new Vector<FileStruct>();
		output_num = new Vector<Long>();
		try {
			FileInputStream inFileStream = new FileInputStream("src/Config.properties");
			app_properties.load(inFileStream);
			inDirectorys = app_properties.getProperty("InDirectorys").split(";");
			out_put_url = app_properties.getProperty("OutDirector");
		} catch (IOException e) {
			e.printStackTrace();
		}

		Thread resolve_thread = new analysiser();
		resolve_thread.start();

		Thread outFile_thread = new outputFile();
		outFile_thread.start();
	}
}
