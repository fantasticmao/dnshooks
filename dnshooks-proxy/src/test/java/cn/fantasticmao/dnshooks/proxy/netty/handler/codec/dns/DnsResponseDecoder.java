package cn.fantasticmao.dnshooks.proxy.netty.handler.codec.dns;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.CorruptedFrameException;
import io.netty.handler.codec.dns.*;

import java.net.SocketAddress;

import static io.netty.util.internal.ObjectUtil.checkNotNull;

/**
 * Copy from {@link io.netty.handler.codec.dns.DnsResponseDecoder}
 */
public abstract class DnsResponseDecoder<A extends SocketAddress> {

    private final DnsRecordDecoder recordDecoder;

    /**
     * Creates a new decoder with the specified {@code recordDecoder}.
     */
    public DnsResponseDecoder(DnsRecordDecoder recordDecoder) {
        this.recordDecoder = checkNotNull(recordDecoder, "recordDecoder");
    }

    public final DnsResponse decode(A sender, A recipient, ByteBuf buffer) throws Exception {
        final int id = buffer.readUnsignedShort();

        final int flags = buffer.readUnsignedShort();
        if (flags >> 15 == 0) {
            throw new CorruptedFrameException("not a response");
        }

        final DnsResponse response = newResponse(
            sender,
            recipient,
            id,
            DnsOpCode.valueOf((byte) (flags >> 11 & 0xf)), DnsResponseCode.valueOf((byte) (flags & 0xf)));

        response.setRecursionDesired((flags >> 8 & 1) == 1);
        response.setAuthoritativeAnswer((flags >> 10 & 1) == 1);
        response.setTruncated((flags >> 9 & 1) == 1);
        response.setRecursionAvailable((flags >> 7 & 1) == 1);
        response.setZ(flags >> 4 & 0x7);

        boolean success = false;
        try {
            final int questionCount = buffer.readUnsignedShort();
            final int answerCount = buffer.readUnsignedShort();
            final int authorityRecordCount = buffer.readUnsignedShort();
            final int additionalRecordCount = buffer.readUnsignedShort();

            decodeQuestions(response, buffer, questionCount);
            if (!decodeRecords(response, DnsSection.ANSWER, buffer, answerCount)) {
                success = true;
                return response;
            }
            if (!decodeRecords(response, DnsSection.AUTHORITY, buffer, authorityRecordCount)) {
                success = true;
                return response;
            }

            decodeRecords(response, DnsSection.ADDITIONAL, buffer, additionalRecordCount);
            success = true;
            return response;
        } finally {
            if (!success) {
                response.release();
            }
        }
    }

    protected abstract DnsResponse newResponse(A sender, A recipient, int id,
                                               DnsOpCode opCode, DnsResponseCode responseCode) throws Exception;

    private void decodeQuestions(DnsResponse response, ByteBuf buf, int questionCount) throws Exception {
        for (int i = questionCount; i > 0; i--) {
            response.addRecord(DnsSection.QUESTION, recordDecoder.decodeQuestion(buf));
        }
    }

    private boolean decodeRecords(
        DnsResponse response, DnsSection section, ByteBuf buf, int count) throws Exception {
        for (int i = count; i > 0; i--) {
            final DnsRecord r = recordDecoder.decodeRecord(buf);
            if (r == null) {
                // Truncated response
                return false;
            }

            response.addRecord(section, r);
        }
        return true;
    }
}
