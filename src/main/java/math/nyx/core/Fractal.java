package math.nyx.core;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import math.nyx.framework.FractalCodec;
import math.nyx.framework.Transform;

import com.google.common.base.Objects;

public class Fractal implements Serializable {
	private static final long serialVersionUID = -3691227743645881425L;

	private String codecName;
	
	private int signalDimension;

	private final List<Transform> transforms = new LinkedList<Transform>();

	public Signal decode() {
		return decode(1);
	}

	public Signal decode(int scale) {
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("applicationContext.xml");
		FractalCodec codec = (FractalCodec)ctx.getBean(codecName);
		Signal signal = codec.decode(this, scale);
		ctx.close();
		return signal;
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

	public void addTransform(Transform transform) {
		transforms.add(transform);
	}

	public List<Transform> getTransforms() {
		return transforms;
	}

	@Override
	public String toString() {
	    return Objects.toStringHelper(this.getClass()).add("transforms", transforms)
	            .toString();
	}
}
