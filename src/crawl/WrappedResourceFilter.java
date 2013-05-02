package crawl;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

public class WrappedResourceFilter implements Filter {
	ServletContext sc;

	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse rsp,
			FilterChain chain) throws IOException, ServletException {
		HttpServletRequest servletReq = (HttpServletRequest) req;
		String wrappedId = "/wrapped/";
		int idx = servletReq.getRequestURI().indexOf(wrappedId);
		if (idx == -1) {
			chain.doFilter(req, rsp);
			return;
		}
		String resourceId = servletReq.getRequestURI().substring(
				idx + wrappedId.length());
		try {
			LinkedHashMap wrappedResource = (LinkedHashMap) servletReq
					.getSession().getAttribute("WrappedResource");
			if (wrappedResource == null) {
				wrappedResource = WrappedResource
						.restore(
								sc.getRealPath(WrappedResource
										.getFileName(resourceId)))
						.getResources();
				servletReq.getSession().setAttribute("WrappedResource",
						wrappedResource);
			}
			rsp.getOutputStream().write(
					(byte[]) wrappedResource.get(resourceId));
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		rsp.getOutputStream().flush();
	}

	@Override
	public void init(FilterConfig arg0) throws ServletException {
		// TODO Auto-generated method stub
		sc = arg0.getServletContext();
	}

}
