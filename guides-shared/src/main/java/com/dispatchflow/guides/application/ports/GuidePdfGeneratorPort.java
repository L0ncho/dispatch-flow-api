package com.dispatchflow.guides.application.ports;

import com.dispatchflow.guides.domain.entities.DispatchGuide;

public interface GuidePdfGeneratorPort {

    byte[] generate(DispatchGuide guide);
}
