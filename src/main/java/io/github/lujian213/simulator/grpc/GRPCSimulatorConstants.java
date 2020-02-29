package io.github.lujian213.simulator.grpc;

import io.github.lujian213.simulator.SimSimulatorConstants;

public class GRPCSimulatorConstants extends SimSimulatorConstants {
	public static final String PROP_NAME_PORT = "simulator.grpc.port";
	public static final String PROP_NAME_USE_SSL = "simulator.grpc.useSSL";
	public static final String PROP_NAME_HANDLER_CLASSNAME = "simulator.grpc.handler.classname";
	public static final String PROP_NAME_HANDLER_SIM_CLASSNAME = "simulator.grpc.handler.sim.classname";
	public static final String PROP_NAME_CLIENT_CLASSNAME = "simulator.grpc.client.classname";
	public static final String PROP_NAME_CERTCHAIN_FILE = "simulator.grpc.certchain.file";
	public static final String PROP_NAME_PRIVATEKEY_FILE = "simulator.grpc.privatekey.file";
	public static final String PROP_NAME_TRUSTCERT_FILE = "simulator.grpc.trustcert.file";
	public static final String PROP_NAME_CLIENT_AUTHORITY = "simulator.grpc.client.authority";
}
