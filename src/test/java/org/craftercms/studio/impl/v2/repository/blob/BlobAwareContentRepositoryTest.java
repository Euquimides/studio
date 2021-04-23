/*
 * Copyright (C) 2007-2020 Crafter Software Corporation. All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as published by
 * the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.craftercms.studio.impl.v2.repository.blob;

import org.apache.commons.io.FilenameUtils;
import org.craftercms.commons.config.ConfigurationException;
import org.craftercms.studio.api.v1.dal.DeploymentSyncHistory;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.repository.RepositoryItem;
import org.craftercms.studio.api.v1.service.deployment.DeploymentException;
import org.craftercms.studio.api.v1.to.DeploymentItemTO;
import org.craftercms.studio.api.v1.to.VersionTO;
import org.craftercms.studio.api.v2.repository.blob.StudioBlobStore;
import org.craftercms.studio.api.v2.repository.blob.StudioBlobStoreResolver;
import org.craftercms.studio.impl.v2.repository.GitContentRepository;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static java.time.ZonedDateTime.now;
import static java.util.Collections.singletonList;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.testng.Assert.*;

/**
 * @author joseross
 */
public class BlobAwareContentRepositoryTest {

    public static final String SITE = "test";
    public static final String PARENT_PATH = "/static-assets";
    public static final String ORIGINAL_PATH = PARENT_PATH + "/test.txt";
    public static final String BLOB_EXT = "blob";
    public static final String POINTER_PATH = ORIGINAL_PATH + "." + BLOB_EXT;
    public static final String FOLDER_PATH = PARENT_PATH + "/folder";
    public static final String NEW_FOLDER_PATH = FOLDER_PATH + "2";
    public static final String NEW_FILE_PATH = FOLDER_PATH + "/test.txt";
    public static final String NEW_POINTER_PATH = NEW_FILE_PATH + "." + BLOB_EXT;
    public static final String NO_EXT_PATH = PARENT_PATH + "/test";
    public static final ByteArrayInputStream CONTENT = new ByteArrayInputStream("test".getBytes());
    public static final ByteArrayInputStream POINTER = new ByteArrayInputStream("pointer".getBytes());
    public static final long SIZE = 42;
    public static final String USER = "John Doe";
    public static final String ENV = "live";
    public static final String COMMENT = "Going live!";
    public static final String STORE_ID = "BLOB_STORE";
    public static final String LOCAL_PATH = "/site/website/index.xml";

    @InjectMocks
    private BlobAwareContentRepository proxy;

    @Mock
    private org.craftercms.studio.impl.v1.repository.git.GitContentRepository localV1;

    @Mock
    private GitContentRepository localV2;

    @Mock
    private StudioBlobStore store;

    @Mock
    private StudioBlobStoreResolver resolver;

    @Captor
    private ArgumentCaptor<List<DeploymentItemTO>> itemsCaptor;

    @BeforeMethod
    public void setUp() throws Exception {
        initMocks(this);

        when(store.getId()).thenReturn(STORE_ID);

        when(resolver.getByPaths(SITE, FOLDER_PATH)).thenReturn(store);
        when(resolver.getByPaths(SITE, ORIGINAL_PATH)).thenReturn(store);
        when(resolver.getByPaths(SITE, ORIGINAL_PATH, NEW_FILE_PATH)).thenReturn(store);
        when(resolver.getByPaths(SITE, FOLDER_PATH, NEW_FOLDER_PATH)).thenReturn(store);
        when(resolver.getByPaths(SITE, NO_EXT_PATH)).thenReturn(store);

        when(localV1.contentExists(SITE, NEW_FILE_PATH)).thenReturn(false);
        when(localV1.isFolder(SITE, NEW_FILE_PATH)).thenReturn(false);

        when(localV1.isFolder(SITE, FOLDER_PATH)).thenReturn(true);
        when(localV1.isFolder(SITE, NEW_FOLDER_PATH)).thenReturn(false);

        when(localV1.contentExists(SITE, ORIGINAL_PATH)).thenReturn(false);
        when(localV1.contentExists(SITE, POINTER_PATH)).thenReturn(true);
        when(localV1.getContent(SITE, POINTER_PATH)).thenReturn(POINTER);
        when(localV1.isFolder(SITE, PARENT_PATH)).thenReturn(true);

        Map<String, String> delta = new TreeMap<>();
        delta.put(POINTER_PATH, "C");
        delta.put("/unrelated/file.xml", "U");
        when(localV2.getChangeSetPathsFromDelta(SITE, null, null)).thenReturn(delta);

        when(store.contentExists(SITE, ORIGINAL_PATH)).thenReturn(true);
        when(store.contentExists(SITE, POINTER_PATH)).thenReturn(false);
        when(store.getContent(SITE, ORIGINAL_PATH)).thenReturn(CONTENT);
        when(store.getContentSize(SITE, ORIGINAL_PATH)).thenReturn(SIZE);
        when(store.isFolder(SITE, PARENT_PATH)).thenReturn(false);
        when(store.isFolder(SITE, ORIGINAL_PATH)).thenReturn(false);

        when(store.copyContent(SITE, FOLDER_PATH, NEW_FOLDER_PATH)).thenReturn("something");

        proxy.setFileExtension(BLOB_EXT);
        proxy.setInterceptedPaths(new String[]{ "/static-assets/.*" });
    }

    @Test
    public void configShouldNotBeIntercepted() throws ConfigurationException, IOException, ServiceLayerException {
        proxy.contentExists(SITE, "/config/studio/site-config.xml");

        verify(resolver, never()).getByPaths(any(), any());
    }

    @Test
    public void contentExistsTest() {
        assertTrue(proxy.contentExists(SITE, ORIGINAL_PATH), "original path should exist");
    }

    @Test
    public void getContentTest() {
        assertEquals(proxy.getContent(SITE, ORIGINAL_PATH), CONTENT, "original path should return the original content");
    }

    @Test
    public void getContentSizeTest() {
        assertEquals(proxy.getContentSize(SITE, ORIGINAL_PATH), SIZE, "original path should return the original size");
    }

    @Test
    public void writeContentTest() throws ServiceLayerException {
        proxy.writeContent(SITE, ORIGINAL_PATH, CONTENT);

        verify(store).writeContent(SITE, ORIGINAL_PATH, CONTENT);
        verify(localV1).writeContent(eq(SITE), eq(POINTER_PATH), any());
    }

    @Test
    public void writeContentFailTest() throws ServiceLayerException {
        when(store.writeContent(SITE, ORIGINAL_PATH, CONTENT)).thenThrow(new ServiceLayerException("Test"));

        try {
            proxy.writeContent(SITE, ORIGINAL_PATH, CONTENT);
        } catch (Exception e) {
            // expected
        }

        verify(store).writeContent(SITE, ORIGINAL_PATH, CONTENT);
        verify(localV1, never()).writeContent(eq(SITE), eq(POINTER_PATH), any());
    }

    @Test
    public void deleteFileTest() {
        when(store.deleteContent(SITE, ORIGINAL_PATH, USER)).thenReturn(EMPTY);

        proxy.deleteContent(SITE, ORIGINAL_PATH, USER);

        verify(store).deleteContent(SITE, ORIGINAL_PATH, USER);
        verify(localV1).deleteContent(SITE, POINTER_PATH, USER);
    }

    @Test
    public void deleteFolderTest() {
        proxy.deleteContent(SITE, FOLDER_PATH, USER);

        verify(store).deleteContent(SITE, FOLDER_PATH, USER);
        verify(localV1).deleteContent(SITE, FOLDER_PATH, USER);
    }

    @Test
    public void deleteContentFailTest() {
        when(store.deleteContent(SITE, ORIGINAL_PATH, USER)).thenReturn(null);

        proxy.deleteContent(SITE, ORIGINAL_PATH, USER);

        verify(localV1, never()).deleteContent(SITE, POINTER_PATH, USER);
    }

    @Test
    public void moveFileTest() {
        proxy.moveContent(SITE, ORIGINAL_PATH, NEW_FILE_PATH);

        verify(store).moveContent(SITE, ORIGINAL_PATH, NEW_FILE_PATH, null);
        verify(localV1).moveContent(SITE, POINTER_PATH, NEW_POINTER_PATH, null);
    }

    @Test
    public void moveFolderTest() {
        proxy.moveContent(SITE, FOLDER_PATH, NEW_FOLDER_PATH);

        verify(store).moveContent(SITE, FOLDER_PATH, NEW_FOLDER_PATH, null);
        verify(localV1).moveContent(SITE, FOLDER_PATH, NEW_FOLDER_PATH, null);
    }

    @Test
    public void copyFileTest() {
        when(store.copyContent(SITE, ORIGINAL_PATH, NEW_FILE_PATH)).thenReturn(EMPTY);

        proxy.copyContent(SITE, ORIGINAL_PATH, NEW_FILE_PATH);

        verify(store).copyContent(SITE, ORIGINAL_PATH, NEW_FILE_PATH);
        verify(localV1).copyContent(SITE, POINTER_PATH, NEW_POINTER_PATH);
    }

    @Test
    public void copyFolderTest() {
        proxy.copyContent(SITE, FOLDER_PATH, NEW_FOLDER_PATH);

        verify(store).copyContent(SITE, FOLDER_PATH, NEW_FOLDER_PATH);
        verify(localV1).copyContent(SITE, FOLDER_PATH, NEW_FOLDER_PATH);
    }

    @Test
    public void getContentChildrenWithoutRemoteTest() {
        RepositoryItem item = new RepositoryItem();
        item.path = ORIGINAL_PATH;
        when(localV1.getContentChildren(SITE, PARENT_PATH)).thenReturn(new RepositoryItem[] { item });

        RepositoryItem[] result = proxy.getContentChildren(SITE, PARENT_PATH);

        assertNotNull(result);
        assertEquals(result.length, 1);
        assertEquals(result[0].path, ORIGINAL_PATH);
    }

    @Test
    public void getContentChildrenWithRemoteTest() {
        RepositoryItem item = new RepositoryItem();
        item.path = PARENT_PATH;
        item.name = FilenameUtils.getName(POINTER_PATH);
        when(localV1.getContentChildren(SITE, PARENT_PATH)).thenReturn(new RepositoryItem[] { item });

        RepositoryItem[] result = proxy.getContentChildren(SITE, PARENT_PATH);

        assertNotNull(result);
        assertEquals(result.length, 1);
        assertEquals(result[0].path, PARENT_PATH);
        assertEquals(result[0].name, FilenameUtils.getName(ORIGINAL_PATH));
    }

    @Test
    public void isFolderTest() {
        assertTrue(proxy.isFolder(SITE, PARENT_PATH), "parent path should be recognized as folder");
        assertFalse(proxy.isFolder(SITE, ORIGINAL_PATH), "original path should be recognized as file");
    }

    @Test
    public void publishRemoteFileTest() throws DeploymentException {
        DeploymentItemTO item = new DeploymentItemTO();
        item.setSite(SITE);
        item.setPath(ORIGINAL_PATH);
        List<DeploymentItemTO> items = singletonList(item);
        proxy.publish(SITE, EMPTY, items, ENV, USER, COMMENT);

        DeploymentItemTO pointerItem = new DeploymentItemTO();
        pointerItem.setSite(SITE);
        pointerItem.setPath(POINTER_PATH);

        verify(store).publish(eq(SITE), eq(EMPTY), itemsCaptor.capture(), eq(ENV), eq(USER), eq(COMMENT));
        assertEquals(itemsCaptor.getValue().get(0), item);

        verify(localV2).publish(eq(SITE), eq(EMPTY), itemsCaptor.capture(), eq(ENV), eq(USER), eq(COMMENT));
        assertEquals(itemsCaptor.getValue().get(0), pointerItem);
    }

    @Test
    public void publishLocalFileTest() throws DeploymentException {
        DeploymentItemTO item = new DeploymentItemTO();
        item.setSite(SITE);
        item.setPath(LOCAL_PATH);
        List<DeploymentItemTO> items = singletonList(item);
        proxy.publish(SITE, EMPTY, items, ENV, USER, COMMENT);

        DeploymentItemTO pointerItem = new DeploymentItemTO();
        pointerItem.setSite(SITE);
        pointerItem.setPath(LOCAL_PATH);

        verify(store, never()).publish(any(), any(), any(), any(), any(), any());

        verify(localV2).publish(eq(SITE), eq(EMPTY), itemsCaptor.capture(), eq(ENV), eq(USER), eq(COMMENT));
        assertEquals(itemsCaptor.getValue().get(0), pointerItem);
    }

    @Test
    public void publishMixFilesTest() throws DeploymentException {
        DeploymentItemTO remoteItem = new DeploymentItemTO();
        remoteItem.setSite(SITE);
        remoteItem.setPath(ORIGINAL_PATH);

        DeploymentItemTO localItem = new DeploymentItemTO();
        localItem.setSite(SITE);
        localItem.setPath(LOCAL_PATH);

        DeploymentItemTO pointerItem = new DeploymentItemTO();
        pointerItem.setSite(SITE);
        pointerItem.setPath(POINTER_PATH);

        List<DeploymentItemTO> items = Arrays.asList(remoteItem, localItem);
        proxy.publish(SITE, EMPTY, items, ENV, USER, COMMENT);

        verify(store).publish(eq(SITE), eq(EMPTY), itemsCaptor.capture(), eq(ENV), eq(USER), eq(COMMENT));
        assertTrue(itemsCaptor.getValue().contains(remoteItem), "remote file should have been published");

        verify(localV2).publish(eq(SITE), eq(EMPTY), itemsCaptor.capture(), eq(ENV), eq(USER), eq(COMMENT));
        assertTrue(itemsCaptor.getValue().contains(pointerItem), "pointer file should have been published");

        verify(localV2).publish(eq(SITE), eq(EMPTY), itemsCaptor.capture(), eq(ENV), eq(USER), eq(COMMENT));
        assertTrue(itemsCaptor.getValue().contains(localItem), "local file should have been published");
    }

    @Test
    public void getDeploymentHistoryTest() {
        DeploymentSyncHistory history = new DeploymentSyncHistory();
        history.setPath(POINTER_PATH);

        when(localV2.getDeploymentHistory(eq(SITE), any(), any(), any(), any(), any(), anyInt()))
                .thenReturn(singletonList(history));

        List<DeploymentSyncHistory> result =
                proxy.getDeploymentHistory(SITE, singletonList(ENV), now(), now(), null, null, 10);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(result.get(0).getPath(), ORIGINAL_PATH);
    }

    @Test
    public void getContentVersionHistoryTest() {
        VersionTO version1 = new VersionTO();
        VersionTO version2 = new VersionTO();

        when(localV1.getContentVersionHistory(eq(SITE), eq(POINTER_PATH)))
                .thenReturn(new VersionTO[] { version1, version2 });

        VersionTO[] versions = proxy.getContentVersionHistory(SITE, ORIGINAL_PATH);

        assertNotNull(versions);
        assertEquals(versions.length, 2);
    }

    @Test
    public void fileWithoutExtensionTest() throws ServiceLayerException {
        proxy.writeContent(SITE, NO_EXT_PATH, CONTENT);

        verify(store).writeContent(SITE, NO_EXT_PATH, CONTENT);
        verify(localV1).writeContent(eq(SITE), eq(NO_EXT_PATH + "." + BLOB_EXT), any());
    }

    @Test void getChangeSetPathsFromDeltaTest() {
        Map<String, String> result = proxy.getChangeSetPathsFromDelta(SITE, null, null);

        assertEquals(result.size(), 2);
        assertTrue(result.containsKey(ORIGINAL_PATH), "the blob extension should have been removed");
    }

}
