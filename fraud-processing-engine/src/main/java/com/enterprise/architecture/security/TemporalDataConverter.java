package com.enterprise.architecture.security;

import io.temporal.api.common.v1.Payload;
// 👑 FIX: Corrected package path for the PayloadCodec interface layer
import io.temporal.payload.codec.PayloadCodec;
import com.google.protobuf.ByteString;
import javax.annotation.Nonnull;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Verified Cryptographic payload interceptor with corrected SDK package paths.
 */
public class TemporalDataConverter implements PayloadCodec {

    private static final byte[] AES_KEY_256 = new byte[] {
            0x5f, 0x1a, 0x7c, 0x3d, 0x4e, 0x2b, 0x1f, 0x6a,
            0x3e, 0x2d, 0x7b, 0x4c, 0x1a, 0x5e, 0x6f, 0x2b,
            0x4a, 0x3c, 0x2d, 0x1e, 0x5f, 0x6b, 0x7a, 0x4d,
            0x1c, 0x2e, 0x3f, 0x4a, 0x5b, 0x6c, 0x7d, 0x1e
    };

    private static final String ENCODING_KEY = "encoding";
    private static final String ENCODING_VALUE = "binary/encrypted-aes256";

    @Nonnull
    @Override
    public List<Payload> encode(@Nonnull List<Payload> payloads) {
        return payloads.stream().map(payload -> {
            try {
                byte[] rawBytes = payload.toByteArray();
                byte[] encryptedBytes = EncryptionUtil.encrypt(rawBytes, AES_KEY_256);
                return Payload.newBuilder()
                        .putMetadata(ENCODING_KEY, ByteString.copyFromUtf8(ENCODING_VALUE))
                        .setData(ByteString.copyFrom(encryptedBytes))
                        .build();
            } catch (Exception e) {
                throw new RuntimeException("AES encryption failed during encoding.", e);
            }
        }).collect(Collectors.toList());
    }

    @Nonnull
    @Override
    public List<Payload> decode(@Nonnull List<Payload> payloads) {
        return payloads.stream().map(payload -> {
            String encoding = payload.getMetadataOrDefault(ENCODING_KEY, ByteString.EMPTY).toStringUtf8();
            if (!ENCODING_VALUE.equals(encoding)) {
                return payload;
            }
            try {
                byte[] encryptedBytes = payload.getData().toByteArray();
                byte[] decryptedBytes = EncryptionUtil.decrypt(encryptedBytes, AES_KEY_256);
                return Payload.parseFrom(decryptedBytes);
            } catch (Exception e) {
                throw new RuntimeException("AES decryption failed during decoding.", e);
            }
        }).collect(Collectors.toList());
    }
}
