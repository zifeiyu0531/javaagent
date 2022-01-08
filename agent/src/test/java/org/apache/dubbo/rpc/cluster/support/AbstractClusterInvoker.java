package org.apache.dubbo.rpc.cluster.support;

import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.RpcException;
import org.apache.dubbo.rpc.cluster.ClusterInvoker;
import org.apache.dubbo.rpc.cluster.Directory;

import java.util.List;

public abstract class AbstractClusterInvoker<T> implements ClusterInvoker<T> {
    protected Directory<T> directory;

    protected List<Invoker<T>> list(Invocation invocation) throws RpcException {
        return directory.list(invocation);
    }
}
