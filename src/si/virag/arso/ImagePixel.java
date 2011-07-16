package si.virag.arso;

public class ImagePixel {
	private final byte R;
	private final byte G;
	private final byte B;
	
	public ImagePixel(int r, int g, int b) {
		super();
		R = (byte) r;
		G = (byte) g;
		B = (byte) b;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + B;
		result = prime * result + G;
		result = prime * result + R;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ImagePixel other = (ImagePixel) obj;
		if (B != other.B)
			return false;
		if (G != other.G)
			return false;
		if (R != other.R)
			return false;
		return true;
	}
}
