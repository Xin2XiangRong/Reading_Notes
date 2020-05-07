package com.spring.example;

import com.grpc.api.RPCDateRequest;
import com.grpc.api.RPCDateResponse;
import com.grpc.api.RPCDateServiceGrpc;
import io.grpc.Channel;
import net.devh.springboot.autoconfigure.grpc.client.GrpcClient;
import org.springframework.stereotype.Service;



/**
 * Created by chaishuai on 2018/6/13.
 */
@Service
public class GrpcClientService {

    @GrpcClient("grpc-server")
    private Channel serverChannel;

    public String sendServerData(String name) {
        RPCDateServiceGrpc.RPCDateServiceBlockingStub rpcDateService = RPCDateServiceGrpc.newBlockingStub(serverChannel);
        //构造请求对象
        RPCDateRequest rpcDateRequest = RPCDateRequest
                .newBuilder()
                .setUserName(name)
                .build();
        RPCDateResponse rpcDateResponse = rpcDateService.getDate(rpcDateRequest);
        return rpcDateResponse.getServerDate();
    }
}
