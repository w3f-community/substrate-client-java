package com.strategyobject.substrateclient.rpc.types;

import com.strategyobject.substrateclient.rpc.core.RpcEncoded;
import com.strategyobject.substrateclient.scale.ScaleEncoded;
import lombok.Getter;

import java.util.Optional;

// RpcEncoder must be overridden. It must be represented in hex string as a byte array of scale.
@Getter
public class Extrinsic<C extends Call, A extends Address, S extends Signature, E extends Extra>
        implements RpcEncoded<Extrinsic<C, A, S, E>>, ScaleEncoded<Extrinsic<C, A, S, E>> {
    private final Optional<SignaturePayload<A, S, E>> signature;
    private final C call;

    Extrinsic(Optional<SignaturePayload<A, S, E>> signature, C call) {
        this.signature = signature;
        this.call = call;
    }

    public static <C extends Call, A extends Address, S extends Signature, E extends Extra> Extrinsic<C, A, S, E> createSigned(
            SignaturePayload<A, S, E> signature,
            C call) {
        return new Extrinsic<>(Optional.of(signature), call);
    }

    public static <C extends Call> Extrinsic<C, ?, ?, ?> createUnsigned(C call) {
        return new Extrinsic<>(Optional.empty(), call);
    }
}