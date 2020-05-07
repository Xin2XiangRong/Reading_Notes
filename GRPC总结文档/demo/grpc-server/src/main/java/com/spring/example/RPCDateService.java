package com.spring.example;

import com.grpc.api.RPCDateRequest;
import com.grpc.api.RPCDateResponse;
import com.grpc.api.RPCDateServiceGrpc;
import io.grpc.stub.StreamObserver;
import net.devh.springboot.autoconfigure.grpc.server.GrpcService;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by chaishuai on 2018/6/13.
 */
@GrpcService(RPCDateServiceGrpc.class)
public class RPCDateService extends RPCDateServiceGrpc.RPCDateServiceImplBase{

    @Override
    public void getDate(RPCDateRequest request, StreamObserver<RPCDateResponse> responseObserver) {
        RPCDateResponse rpcDateResponse = null;
        Date now = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("今天是"+"yyyy年MM月dd日 E kk点mm分");
        String nowTime = simpleDateFormat.format(now);
        try {
            rpcDateResponse = RPCDateResponse
                    .newBuilder()
                    .setServerDate("hello " + request.getUserName() + ", " + nowTime)
                    .build();
        } catch (Exception e) {
            responseObserver.onError(e);
        } finally {
            responseObserver.onNext(rpcDateResponse);
        }
        responseObserver.onCompleted();
    }
}
