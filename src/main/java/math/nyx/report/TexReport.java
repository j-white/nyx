package math.nyx.report;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.math.linear.RealMatrix;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;

public abstract class TexReport {
	private static Logger logger = LogManager.getLogger("Nyx");

	public abstract String getTemplate();

	public void save(File file) throws IOException {
		Configuration cfg = new Configuration();
		cfg.setClassForTemplateLoading(this.getClass(), "");
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
	}
}
