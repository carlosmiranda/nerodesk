/**
 * Copyright (c) 2015, nerodesk.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met: 1) Redistributions of source code must retain the above
 * copyright notice, this list of conditions and the following
 * disclaimer. 2) Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided
 * with the distribution. 3) Neither the name of the nerodesk.com nor
 * the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.nerodesk.om.aws;

import com.amazonaws.services.s3.model.ObjectMetadata;
import com.google.common.collect.Lists;
import com.jcabi.aspects.Tv;
import com.jcabi.s3.Bucket;
import com.jcabi.s3.Ocket;
import com.jcabi.s3.mock.MkBucket;
import com.nerodesk.om.Docs;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;

/**
 * Tests for {@link AwsDocs}.
 *
 * @author Krzysztof Krason (Krzysztof.Krason@gmail.com)
 * @author Felipe Pina (felipe.pina@protonmail.com)
 * @version $Id$
 * @since 0.3
 */
public final class AwsDocsTest {

    /**
     * Temporary folder.
     * @checkstyle VisibilityModifierCheck (3 lines)
     */
    @Rule
    public final transient TemporaryFolder folder = new TemporaryFolder();

    /**
     * AwsDoc can return the size of its contents.
     * @throws Exception If something goes wrong.
     * @todo #131:30min Ideally we should be using mock classes from jcabi-s3 in
     *  this test. However, the ObjectMetadata that is returned by the mocking
     *  framework does not show an accurate contentLength. This is the reason
     *  we're using Mockito. When jcabi-s3 supports this feature (see
     *  https://github.com/jcabi/jcabi-s3/issues/25), let's migrate this test
     *  to com.jcabi.s3.mock classes.
     */
    @Test
    public void fetchesSize() throws Exception {
        final long size = Tv.THOUSAND;
        final Bucket bucket = Mockito.mock(Bucket.class);
        final Ocket ocket = Mockito.mock(Ocket.class);
        Mockito.doReturn(ocket).when(bucket).ocket(Mockito.anyString());
        Mockito.doReturn(Collections.singleton("test"))
            .when(bucket).list(Mockito.anyString());
        final ObjectMetadata meta = new ObjectMetadata();
        meta.setContentLength(size);
        Mockito.doReturn(meta).when(ocket).meta();
        MatcherAssert.assertThat(
            new AwsDocs(bucket, "").size(),
            Matchers.is(size)
        );
    }

    /**
     * AwsDocs conforms to equals and hashCode contract.
     */
    @Test
    public void conformsToEqualsHashCodeContract() {
        EqualsVerifier.forClass(AwsDocs.class)
            .suppress(Warning.TRANSIENT_FIELDS)
            .verify();
    }

    /**
     * AwsDocs can list all docs.
     * @throws IOException If something goes wrong.
     */
    @Test
    public void listsDocs() throws IOException {
        final Bucket bucket = this.mockBucket("lists", "lists-exists");
        final List<String> expected = Lists.newArrayList(bucket.list(""));
        final List<String> names = new AwsDocs(bucket).names();
        MatcherAssert.assertThat(names, Matchers.equalTo(expected));
        MatcherAssert.assertThat(names, Matchers.hasItem("sub/file"));
    }

    /**
     * AwsDocs can find a doc.
     * @throws IOException If something goes wrong.
     */
    @Test
    public void findsDoc() throws IOException {
        final String exists = "finds-exists";
        final Bucket bucket = this.mockBucket("finds", exists);
        final Docs docs = new AwsDocs(bucket);
        MatcherAssert.assertThat(
            docs.doc(exists).exists(),
            Matchers.equalTo(true)
        );
        MatcherAssert.assertThat(
            docs.doc("xyz").exists(),
            Matchers.equalTo(false)
        );
    }

    /**
     * Builds a mock Bucket.
     * @param name Bucket name.
     * @param exists Name of Ocket that exists.
     * @return The mock bucket.
     * @throws IOException If something goes wrong.
     */
    private Bucket mockBucket(final String name, final String exists)
        throws IOException {
        final Path bucket = Files.createDirectories(
            Paths.get(this.folder.getRoot().getAbsolutePath(), name)
        );
        Files.createFile(bucket.resolve(exists));
        Files.createFile(
            Files.createDirectories(bucket.resolve("sub")).resolve("file")
        );
        return new MkBucket(this.folder.getRoot(), name);
    }

}
