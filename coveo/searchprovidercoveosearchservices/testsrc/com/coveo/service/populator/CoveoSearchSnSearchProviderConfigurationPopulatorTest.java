package com.coveo.service.populator;

import com.coveo.model.CoveoSourceModel;
import com.coveo.searchservices.data.CoveoSearchSnSearchProviderConfiguration;
import com.coveo.model.CoveoSearchSnSearchProviderConfigurationModel;
import com.coveo.searchservices.data.CoveoSource;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.apiregistryservices.jalo.ConsumedDestination;
import de.hybris.platform.apiregistryservices.model.ConsumedDestinationModel;
import de.hybris.platform.apiregistryservices.model.DestinationTargetModel;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
@UnitTest
public class CoveoSearchSnSearchProviderConfigurationPopulatorTest {

    @Mock
    Converter<CoveoSourceModel, CoveoSource> sourceConverter;

    @InjectMocks
    CoveoSearchSnSearchProviderConfigurationPopulator populator = new CoveoSearchSnSearchProviderConfigurationPopulator();

    @Before
    public void setUp() {
        List<CoveoSource> sources = new ArrayList<>();
        sources.add(new CoveoSource());
        sources.add(new CoveoSource());
        when(sourceConverter.convertAll(any())).thenReturn(sources);
    }

    @Test
    public void testPopulate() {
        CoveoSearchSnSearchProviderConfiguration target = new CoveoSearchSnSearchProviderConfiguration();
        populator.populate(new CoveoSearchSnSearchProviderConfigurationModel(), target);
        assertEquals(2, target.getSources().size());
    }
}