package com.otognan.driverpete.logic.endpoints;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TrajectoryEndpointStateRepository extends JpaRepository<EndpointProcessorState, Long> {

}
