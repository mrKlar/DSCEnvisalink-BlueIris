package com.envisalink.blueiris;

import com.github.kmbulebu.dsc.it100.IT100;
import com.github.kmbulebu.dsc.it100.commands.read.PartitionArmedCommand;
import com.github.kmbulebu.dsc.it100.commands.read.PartitionDisarmedCommand;
import com.github.kmbulebu.dsc.it100.commands.read.ReadCommand;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import rx.subjects.PublishSubject;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({EnvisalinkListener.class, IT100.class})
public class EnvisalinkListenerTest {

    @Mock
    private Config mockConfig;

    @Mock
    private EnvisalinkEventHandler mockEventHandler;

    @Mock
    private IT100 mockIt100;

    private EnvisalinkListener envisalinkListener;

    private final PublishSubject<ReadCommand> commandSubject = PublishSubject.create();

    @Before
    public void setUp() throws Exception {
        when(mockConfig.getProperty("envisalink.host")).thenReturn("localhost");
        when(mockConfig.getIntProperty("envisalink.port", 4025)).thenReturn(4025);
        when(mockConfig.getProperty("envisalink.password")).thenReturn("password");

        PowerMockito.whenNew(IT100.class).withAnyArguments().thenReturn(mockIt100);

        when(mockIt100.getReadObservable()).thenReturn(commandSubject);

        envisalinkListener = new EnvisalinkListener(mockConfig);
        envisalinkListener.setEventHandler(mockEventHandler);
    }

    @Test
    public void testStart() throws Exception {
        envisalinkListener.start();
        PowerMockito.verifyNew(IT100.class).withArguments(any());
        verify(mockIt100).connect();
    }

    @Test
    public void testStop() throws Exception {
        envisalinkListener.start();
        envisalinkListener.stop();
        verify(mockIt100).disconnect();
    }

    @Test
    public void testPartitionArmAwayEvent() throws Exception {
        envisalinkListener.start();

        PartitionArmedCommand command = mock(PartitionArmedCommand.class);
        when(command.getPartition()).thenReturn(1);
        when(command.getMode()).thenReturn(PartitionArmedCommand.ArmedMode.AWAY);

        commandSubject.onNext(command);

        verify(mockEventHandler).onPartitionArmAway(1);
        verify(mockEventHandler, never()).onPartitionDisarmed(anyInt());
    }

    @Test
    public void testPartitionDisarmedEvent() throws Exception {
        envisalinkListener.start();

        PartitionDisarmedCommand command = mock(PartitionDisarmedCommand.class);
        when(command.getPartition()).thenReturn(2);

        commandSubject.onNext(command);

        verify(mockEventHandler).onPartitionDisarmed(2);
        verify(mockEventHandler, never()).onPartitionArmAway(anyInt());
    }

    @Test
    public void testPartitionArmStayEventIsIgnored() throws Exception {
        envisalinkListener.start();

        PartitionArmedCommand command = mock(PartitionArmedCommand.class);
        when(command.getPartition()).thenReturn(1);
        when(command.getMode()).thenReturn(PartitionArmedCommand.ArmedMode.STAY);

        commandSubject.onNext(command);

        verify(mockEventHandler, never()).onPartitionArmAway(anyInt());
        verify(mockEventHandler, never()).onPartitionDisarmed(anyInt());
    }
}
