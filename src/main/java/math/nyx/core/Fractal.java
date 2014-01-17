package math.nyx.core;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.util.LinkedList;
import java.util.List;

import math.nyx.framework.FractalCodec;
import math.nyx.framework.PartitioningStrategy;

import org.apache.commons.math.linear.RealMatrix;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.util.SerializationUtils;

import com.google.common.base.Objects;

public class Fractal implements Serializable {
	private static final long serialVersionUID = -3691227743645881425L;
	private static Logger logger = LogManager.getLogger("Nyx");

	private String codecName;

	private final String signalClass;

	private final SignalMetadata signalMetadata;

	private final int signalDimension;

	private final int signalPad;

	private final double signalMinVal;

	private final double signalMaxVal;

	private final int numSignalChannels;

	private final List<Transform> transforms = new LinkedList<Transform>();

	public Fractal(Signal signal) {
		signalClass = signal.getClass().getCanonicalName();
		signalMetadata = signal.getMetadata();
		signalDimension = signal.getDimension();
		signalPad = signal.getPad();
		numSignalChannels = signal.getNumChannels();
		signalMinVal = signal.getMinVal();
		signalMaxVal = signal.getMaxVal();
	}

	public Signal decode() {
		return decode(1);
	}

	public Signal decode(int scale) {
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("applicationContext.xml");
		FractalDecoder codec = (FractalDecoder)ctx.getBean(codecName);
		Signal signal = codec.decode(this, scale);
		ctx.close();
		return signal;
	}

	public FractalCodec getCodec() {
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("applicationContext.xml");
		FractalCodec codec = (FractalCodec)ctx.getBean(codecName);
		ctx.close();
		return codec;
	}

	public PartitioningStrategy getPartitioner() {
		return getPartitioner(1);
	}

	public PartitioningStrategy getPartitioner(int scale) {
		return getCodec().getPartitioningStrategy().getPartitioner(this, scale);
	}

	public void setCodecName(String codecName) {
		this.codecName = codecName;
	}

	public String getCodecName() {
		return codecName;
	}

	public int getSignalDimension() {
		return signalDimension;
	}

	public int getSignalPad() {
		return signalPad;
	}

	public int getNumSignalChannels() {
		return numSignalChannels;
	}

	public double getSignalMinVal() {
		return signalMinVal;
	}

	public double getSignalMaxVal() {
		return signalMaxVal;
	}

	public SignalMetadata getSignalMetadata() {
		return signalMetadata;
	}

	public void addTransform(Transform transform) {
		transforms.add(transform);
	}

	public List<Transform> getTransforms() {
		return transforms;
	}

	public static Fractal load(File inputFile) throws IOException {
	    // De-serialize the fractal
	    try(
	    	InputStream file = new FileInputStream(inputFile);
	    	InputStream buffer = new BufferedInputStream(file);
	    	ObjectInput input = new ObjectInputStream (buffer);
	    ){
	    	return (Fractal)input.readObject();
	    }
	    catch(ClassNotFoundException ex) {
	    	System.out.println("Cannot perform input. Class not found: " + ex);
	    	ex.printStackTrace();
	    	throw new IOException(ex);
	    }
	}

	public void write(File outputFile) throws IOException {
		// Serialize the fractal
	    try (
			OutputStream file = new FileOutputStream(outputFile);
			OutputStream buffer = new BufferedOutputStream(file);
			ObjectOutput output = new ObjectOutputStream(buffer);
	    ){
	    	output.writeObject(this);
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Signal getSignalFromDecodedVector(RealMatrix x, int scale) {
		try {
			Class myClass = Class.forName(signalClass);

			Class[] types = {this.getClass(), RealMatrix.class, signalMetadata.getClass()};
			Constructor constructor = myClass.getConstructor(types);

			Object[] parameters = {this, x, signalMetadata.scale(scale)};
			Object instanceOfMyClass = constructor.newInstance(parameters);
			
			return (Signal)instanceOfMyClass;
		} catch(Exception ex) {
			logger.error("Could not create signal from vector.", ex);
			throw new RuntimeException("Could not create signal from vector.", ex);
		}
	}

	public long getSizeInBytes() {
		return SerializationUtils.serialize(this).length;
	}

	@Override
	public String toString() {
	    return Objects.toStringHelper(this.getClass())
	    		.add("codecName", getCodecName())
	    		.add("signalDimension", getSignalDimension())
	    		.add("numSignalChannels", getNumSignalChannels())
	    		.add("numTransforms", getTransforms().size())
	            .toString();
	}
}
