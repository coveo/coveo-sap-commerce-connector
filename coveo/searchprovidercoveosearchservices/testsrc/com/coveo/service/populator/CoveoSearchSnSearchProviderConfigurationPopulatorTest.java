package com.coveo.service.populator;

import com.coveo.model.CoveoSearchSnSearchProviderConfigurationModel;
import com.coveo.model.CoveoSourceModel;
import com.coveo.searchservices.data.CoveoSearchSnSearchProviderConfiguration;
import com.coveo.searchservices.data.CoveoSource;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@UnitTest
public class CoveoSearchSnSearchProviderConfigurationPopulatorTest {

    @Mock
    Converter<CoveoSourceModel, CoveoSource> sourceConverter;

    @InjectMocks
    CoveoSearchSnSearchProviderConfigurationPopulator populator = new CoveoSearchSnSearchProviderConfigurationPopulator();

    @BeforeEach
    void setUp() {
        List<CoveoSource> sources = new ArrayList<>();
        sources.add(new CoveoSource());
        sources.add(new CoveoSource());
        when(sourceConverter.convertAll(any())).thenReturn(sources);
    }

    @Test
    void testPopulate() {
        CoveoSearchSnSearchProviderConfiguration target = new CoveoSearchSnSearchProviderConfiguration();
        populator.populate(new CoveoSearchSnSearchProviderConfigurationModel(), target);
        assertEquals(2, target.getSources().size());
    }
}