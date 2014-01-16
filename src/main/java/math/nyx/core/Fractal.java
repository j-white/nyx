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
import java.util.LinkedList;
import java.util.List;

import math.nyx.framework.FractalCodec;
import math.nyx.framework.PartitioningStrategy;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.google.common.base.Objects;

public class Fractal implements Serializable {
	private static final long serialVersionUID = -3691227743645881425L;

	private String codecName;

	private int signalDimension;

	private int signalPad;

	private int numSignalChannels = 1;

	private final List<Transform> transforms = new LinkedList<Transform>();

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

	public void setSignalDimension(int signalDimension) {
		this.signalDimension = signalDimension;
	}

	public int getSignalPad() {
		return signalPad;
	}

	public void setSignalPad(int signalPad) {
		this.signalPad = signalPad;
	}

	public int getNumSignalChannels() {
		return numSignalChannels;
	}

	public void setNumSignalChannels(int numSignalChannels) {
		this.numSignalChannels = numSignalChannels;
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

	@Override
	public String toString() {
	    return Objects.toStringHelper(this.getClass())
	    		.add("codecName", getCodecName())
	    		.add("signalDimension", getSignalDimension())
	    		.add("numSignalChannels", getNumSignalChannels())
	    		.add("numTransforms", getTransforms().size())
	    		.add("transforms", getTransforms())
	            .toString();
	}
}
