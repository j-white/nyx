package math.nyx.report;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.math.linear.RealMatrix;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import com.google.common.base.Joiner;

import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;

public abstract class TexReport {
	private static Logger logger = LogManager.getLogger("Nyx");

	public abstract String getTemplate();

	private static class ResourceTemplateLoader implements TemplateLoader {
		@Override
		public Object findTemplateSource(String name) throws IOException {
			Resource templateSource = new ClassPathResource("templates/" + name);
			if (!templateSource.exists()) {
				return null;
			} else {
				return templateSource;
			}
		}

		@Override
		public void closeTemplateSource(Object templateSource) throws IOException {
			// This method is intentionally left blank
		}

		@Override
		public long getLastModified(Object templateSource) {
			try {
				return ((Resource)templateSource).lastModified();
			} catch (IOException e) {
				return 0L;
			}
		}

		@Override
		public Reader getReader(Object templateSource, String encoding) throws IOException {
			return new InputStreamReader(((Resource)templateSource).getInputStream(),
					encoding);
		}
	}

	public void save(File file) throws IOException {
		Configuration cfg = new Configuration();
		cfg.setTemplateLoader(new ResourceTemplateLoader());
		cfg.setDefaultEncoding("UTF-8");
	    cfg.setLocale(Locale.US);
	    cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);

		Template template = cfg.getTemplate(getTemplate());
		Map<String, Object> input = new HashMap<String, Object>();
		input.put("report", this);
		input.put("helper", new TexHelper());

	    try (
	    	Writer fileWriter = new FileWriter(file);
	    ){
	    	template.process(input, fileWriter);
	    } catch (TemplateException ex) {
	    	logger.error("Template error.", ex);
        }
	}

	public static class TexHelper {
		public String matrixToTex(RealMatrix m) {
			StringBuilder sb = new StringBuilder();
			int N = m.getRowDimension();
			int M = m.getColumnDimension();
			
			N = Math.min(24, N);
			M = Math.min(24, M);
			
			for (int i = 0; i < N; i++) {
				for (int j = 0; j < M; j++) {
					sb.append(m.getEntry(i, j));
					if (j < (M-1)) {
						sb.append(" & ");
					}
				}
				if (i < (N-1)) {
					sb.append(" \\\\\n");
				}
			}

			return sb.toString();
		}

		public String mapToString(Map<String, Object> map) {
			return Joiner.on(" ").withKeyValueSeparator(":").join(map);
		}
	}
}
