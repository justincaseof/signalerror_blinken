//package vu.de.signalerror;
//
//import org.apache.log4j.Logger;
//import org.osgi.service.component.ComponentContext;
//
//import com.ubigrate.glasnost.adapter.sap.test.TestGUI;
//import com.ubigrate.glasnost.framework.launchd.HardwareInformationAdapter;
//import com.ubigrate.glasnost.framework.launchd.LaunchdService;
//import com.ubigrate.glasnost.framework.osgi.config.LocalConfigurationService;
//import com.ubigrate.glasnost.framework.osgi.config.impl.LogFactoryImpl;
//
//public class SignalErrorApplication
//{
//	private static Logger logger = LogFactoryImpl.getLogger(SignalErrorApplication.class);
//
//	private ComponentContext context;
//	private LaunchdService launchdService;
//	private LocalConfigurationService localConfigService;
//
//	protected void activate( ComponentContext context )
//	{
//		logger.info("Activating.");
//
//		this.context = context;
//
//		this.launchdService = (LaunchdService) context.locateService("LaunchdService");
//		launchdService.getSolutionId();
//
//		localConfigService = (LocalConfigurationService) context.locateService("LocalConfigurationService");
//		localConfigService.setApplicationClass( getClass() );
//
//		new TestGUI();
//
//		System.err.println("### ProcessorId: " + HardwareInformationAdapter.getNativeProcessorId() );
//
//		logger.info("Activated.");
//	}
//
//	protected void deactivate(ComponentContext context)
//	{
//		;
//	}
//
//}
