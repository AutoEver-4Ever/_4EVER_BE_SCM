package org.ever._4ever_be_scm.scm.mm.integration.port;

import java.util.concurrent.CompletableFuture;
import org.ever.event.CreateSupplierUserEvent;
import org.springframework.lang.NonNull;

public interface SupplierUserServicePort {
    @NonNull
    CompletableFuture<Void> createSupplierUser(CreateSupplierUserEvent event);
}
