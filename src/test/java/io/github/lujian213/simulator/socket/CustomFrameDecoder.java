package io.github.lujian213.simulator.socket;

import static io.netty.util.internal.ObjectUtil.checkPositive;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.TooLongFrameException;

public class CustomFrameDecoder extends ByteToMessageDecoder {

	private final Pattern[] delimPatterns;
	private final int maxFrameLength;
	private final boolean stripDelimiter;
	private final boolean failFast;
	private boolean discardingTooLongFrame;
	private int tooLongFrameLength;

//    public CustomFrameDecoder() {
//        this(8192, false, true, "10=.*");
//    }
    public CustomFrameDecoder(int maxFrameLength, String delimRegex) {
        this(maxFrameLength, true, delimRegex);
    }

    public CustomFrameDecoder(
            int maxFrameLength, boolean stripDelimiter, String delimRegex) {
        this(maxFrameLength, stripDelimiter, true, delimRegex);
    }

    public CustomFrameDecoder(
            int maxFrameLength, boolean stripDelimiter, boolean failFast,
            String delimRegex) {
        this(maxFrameLength, stripDelimiter, failFast, new String[] {delimRegex});
    }

    public CustomFrameDecoder(int maxFrameLength, String... delimRegexs) {
        this(maxFrameLength, true, delimRegexs);
    }

    public CustomFrameDecoder(
            int maxFrameLength, boolean stripDelimiter, String... delimRegexs) {
        this(maxFrameLength, stripDelimiter, true, delimRegexs);
    }

	public CustomFrameDecoder(int maxFrameLength, boolean stripDelimiter, boolean failFast, String... delimRegexs) {
		validateMaxFrameLength(maxFrameLength);
		this.delimPatterns = new Pattern[delimRegexs.length];
		for (int i = 0; i < delimRegexs.length; i++) {
			delimPatterns[i] = Pattern.compile(delimRegexs[i]);
		}
		this.maxFrameLength = maxFrameLength;
		this.stripDelimiter = stripDelimiter;
		this.failFast = failFast;
	}

	@Override
	protected final void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		Object decoded = decode(ctx, in);
		if (decoded != null) {
			out.add(decoded);
		}
	}

	protected Object decode(ChannelHandlerContext ctx, ByteBuf buffer) throws Exception {
		// Try all delimiters and choose the delimiter which yields the shortest frame.
		int minFrameLength = Integer.MAX_VALUE;
		int minDelimLength = -1;
		for (Pattern pattern : this.delimPatterns) {
			System.out.println("???????????????????????");
			int[] frameLength = indexOf(buffer, pattern);
			if (frameLength != null && frameLength[0] < minFrameLength) {
				minFrameLength = frameLength[0];
				minDelimLength = frameLength[1] - frameLength[0];
			}
		}

		if (minDelimLength != -1) {
			ByteBuf frame;

			if (discardingTooLongFrame) {
				// We've just finished discarding a very large frame.
				// Go back to the initial state.
				discardingTooLongFrame = false;
				buffer.skipBytes(minFrameLength + minDelimLength);

				int tooLongFrameLength = this.tooLongFrameLength;
				this.tooLongFrameLength = 0;
				if (!failFast) {
					fail(tooLongFrameLength);
				}
				return null;
			}

			if (minFrameLength > maxFrameLength) {
				// Discard read frame.
				buffer.skipBytes(minFrameLength + minDelimLength);
				fail(minFrameLength);
				return null;
			}

			if (stripDelimiter) {
				frame = buffer.readRetainedSlice(minFrameLength);
				buffer.skipBytes(minDelimLength);
			} else {
				frame = buffer.readRetainedSlice(minFrameLength + minDelimLength);
			}

			return frame;
		} else {
			if (!discardingTooLongFrame) {
				if (buffer.readableBytes() > maxFrameLength) {
					// Discard the content of the buffer until a delimiter is found.
					tooLongFrameLength = buffer.readableBytes();
					buffer.skipBytes(buffer.readableBytes());
					discardingTooLongFrame = true;
					if (failFast) {
						fail(tooLongFrameLength);
					}
				}
			} else {
				// Still discarding the buffer since a delimiter is not found.
				tooLongFrameLength += buffer.readableBytes();
				buffer.skipBytes(buffer.readableBytes());
			}
			return null;
		}
	}

	private void fail(long frameLength) {
		if (frameLength > 0) {
			throw new TooLongFrameException(
					"frame length exceeds " + maxFrameLength + ": " + frameLength + " - discarded");
		} else {
			throw new TooLongFrameException("frame length exceeds " + maxFrameLength + " - discarding");
		}
	}

	protected int[] indexOf(ByteBuf haystack, Pattern pattern) {
		byte[] bytes = new byte[haystack.writerIndex() - haystack.readerIndex()];
		for (int i = haystack.readerIndex(); i < haystack.writerIndex(); i++) {
			bytes[i - haystack.readerIndex()] = haystack.getByte(i);
		}

		String str = new String(bytes);
		System.out.println("******************");
		System.out.println(str);
		System.out.println("******************");
		Matcher matcher = pattern.matcher(str);
		if (matcher.find()) {
			return new int[] { matcher.start(), matcher.end() };
		} else {
			return null;
		}
	}

	private static void validateMaxFrameLength(int maxFrameLength) {
		checkPositive(maxFrameLength, "maxFrameLength");
	}

	public static void main(String[] args) {
		String input = "8=FIX 4.49=114535=AR49=16378110=000";
		String regexStr = "10=.*";
		Pattern pattern = Pattern.compile(regexStr);
		Matcher matcher = pattern.matcher(input);
		while (matcher.find()) {
			System.out.println(matcher.start());
			System.out.println(matcher.end());
		}
	}
}
