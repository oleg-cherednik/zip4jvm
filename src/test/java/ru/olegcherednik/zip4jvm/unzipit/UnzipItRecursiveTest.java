/*
 * Copyright 2019 Oleg Cherednik (oleg.cherednik@gmail.com)
 *
 * Licensed under The Apache Software License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package ru.olegcherednik.zip4jvm.unzipit;

import ru.olegcherednik.zip4jvm.BaseTest;
import ru.olegcherednik.zip4jvm.UnzipIt;
import ru.olegcherednik.zip4jvm.Zip4jvmSuite;
import ru.olegcherednik.zip4jvm.model.settings.UnzipSettings;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.nio.file.Path;

import static ru.olegcherednik.zip4jvm.assertj.Zip4jvmAssertions.assertThatDirectory;

/**
 * For all tests we have the same zip file. Items like {@code *.zip} is archives.
 * <pre>
 * >recursive.zip
 * > 0    1st  2nd  3rd  4th <-- recursiveLevel
 * > |-- aa
 * > |    |-- bb
 * > |    |    |-- group.zip
 * > |    |    |    |-- five_six.zip
 * > |    |    |    |    |-- five.txt
 * > |    |    |    |    |-- six.txt
 * > |    |    |    |-- seven_eight.zip
 * > |    |    |    |    |-- seven.txt
 * > |    |    |    |    |-- eight.txt
 * > |    |    |    |-- five_six_seven_eight.txt
 * > |    |    |-- group_group.txt
 * > |-- one_two.zip
 * > |    |-- one.zip
 * > |    |    |-- one.txt
 * > |    |    |-- ont_one.txt
 * > |    |-- two.zip
 * > |    |    |-- two.txt
 * > |    |    |-- two_two.txt
 * > |    |-- one_two.txt
 * > |-- three_four.zip
 * > |    |-- three.zip
 * > |    |    |-- three.txt
 * > |    |    |-- three_four.txt
 * > |    |-- four.zip
 * > |    |    |-- four.txt
 * > |    |    |-- four_four.txt
 * > |    |-- three_four.txt
 * > |-- onw_two_three_four.txt
 * </pre>
 *
 * @author Oleg Cherednik
 * @since 13.09.2025
 */
@Test
public class UnzipItRecursiveTest extends BaseTest {

    private final Path zip = Zip4jvmSuite.getResourcePath("zip/recursive.zip");

    /**
     * By default, or when we set {@code recursiveLevel = UnzipSettings.RECURSIVE_LEVEL_OFF},
     * none included zip should be extracted. As result, we should have the
     * following content on the disc.
     * <pre>
     * >recursive
     * > |-- aa
     * > |    |-- bb
     * > |    |    |-- group.zip
     * > |    |    |-- group_group.txt
     * > |-- one_two.zip
     * > |-- three_four.zip
     * > |-- onw_two_three_four.txt
     * </pre>
     */
    @Test(dataProvider = "recursiveLevelOff")
    public void shouldUnzipRecursiveOffWhenDefaultSettings(Integer recursiveLevel) {
        Path dstDir = getTestRoot();
        UnzipSettings settings =
                recursiveLevel == null ? UnzipSettings.DEFAULT
                                       : UnzipSettings.builder().recursiveLevel(recursiveLevel).build();

        UnzipIt.zip(zip).settings(settings).dstDir(dstDir).extract();

        assertThatDirectory(dstDir)
                .hasOnlyDirectoriesRegularFiles(1, 3)
                .withDirectory("aa", dir1 ->
                        dir1.hasOnlyDirectories(1)
                            .withDirectory("bb", dir2 ->
                                    dir2.hasOnlyRegularFiles(2)
                                        .withRegularFile("group.zip", file -> file.hasSize(763))
                                        .withRegularFile("group_group.txt", file -> file.hasSize(11))))
                .withRegularFile("one_two.zip", file -> file.hasSize(841))
                .withRegularFile("three_four.zip", file -> file.hasSize(862))
                .withRegularFile("one_two_three_four.txt", file -> file.hasSize(7));
    }

    @DataProvider(name = "recursiveLevelOff")
    public static Object[][] recursiveLevelOff() {
        return new Object[][] {
                { null },
                { UnzipSettings.RECURSIVE_LEVEL_OFF }
        };
    }

    /**
     * When we set {@code recursiveLevel = 1}, only up to 1st level zip files
     * should be extracted. In the current example, there are:
     * <ul>
     * <li>{@code one_two.zip}</li>
     * <li>{@code three_four.zip}</li>
     * </ul>
     * As result, we should have the following content on the disc.
     * <pre>
     * >recursive
     * > |-- aa
     * > |    |-- bb
     * > |    |    |-- group.zip
     * > |    |    |-- group_group.txt
     * > |-- one_two
     * > |    |-- one.zip
     * > |    |-- two.zip
     * > |    |-- one_two.txt
     * > |-- three_four
     * > |    |-- three.zip
     * > |    |-- four.zip
     * > |    |-- three_four.txt
     * </pre>
     */
    public void shouldUnzipUpToFirstLevelWhenRecursiveLevelOne() {
        Path dstDir = getTestRoot();
        UnzipSettings settings = UnzipSettings.builder().recursiveLevel(1).build();

        UnzipIt.zip(zip).settings(settings).dstDir(dstDir).extract();

        assertThatDirectory(dstDir)
                .hasOnlyDirectoriesRegularFiles(3, 1)
                .withDirectory("aa", dir1 ->
                        dir1.hasOnlyDirectories(1)
                            .withDirectory("bb", dir2 ->
                                    dir2.hasOnlyRegularFiles(2)
                                        .withRegularFile("group.zip", file -> file.hasSize(763))
                                        .withRegularFile("group_group.txt", file -> file.hasSize(11))))
                .withDirectory("one_two", dir2 ->
                        dir2.hasOnlyRegularFiles(3)
                            .withRegularFile("one.zip", file -> file.hasSize(362))
                            .withRegularFile("two.zip", file -> file.hasSize(362))
                            .withRegularFile("one_two.txt", file -> file.hasSize(3)))
                .withDirectory("three_four", dir2 ->
                        dir2.hasOnlyRegularFiles(3)
                            .withRegularFile("three.zip", file -> file.hasSize(374))
                            .withRegularFile("four.zip", file -> file.hasSize(368))
                            .withRegularFile("three_four.txt", file -> file.hasSize(3)));
    }

    /**
     * When we set {@code recursiveLevel = 2}, only up to 2nd level zip files
     * should be extracted. In the current example, there are:
     * <ul>
     * <li>{@code one_two.zip} as 1st level</li>
     * <li>{@code three_four.zip} as 1st level</li>
     * <li>{@code one.zip} as 2nd level</li>
     * <li>{@code two.zip} as 2nd level</li>
     * <li>{@code three.zip} as 2nd level</li>
     * <li>{@code four.zip} as 2nd level</li>
     * </ul>
     * As result, we should have the following content on the disc.
     * <pre>
     * >recursive
     * > |-- aa
     * > |    |-- bb
     * > |    |    |-- group.zip
     * > |    |    |-- group_group.txt
     * > |-- one_two
     * > |    |-- one
     * > |    |    |-- one.txt
     * > |    |    |-- one_one.txt
     * > |    |-- two
     * > |    |    |-- two.txt
     * > |    |    |-- tow_two.txt
     * > |    |-- one_two.txt
     * > |-- three_four
     * > |    |-- three
     * > |    |    |-- three.txt
     * > |    |    |-- three_three.txt
     * > |    |-- four
     * > |    |    |-- four.txt
     * > |    |    |-- four_four.txt
     * > |    |-- three_four.txt
     * </pre>
     */
    public void shouldUnzipUpToSecondLevelWhenRecursiveLevelTwo() {
        Path dstDir = getTestRoot();
        UnzipSettings settings = UnzipSettings.builder().recursiveLevel(2).build();

        UnzipIt.zip(zip).settings(settings).dstDir(dstDir).extract();

        assertThatDirectory(dstDir)
                .hasOnlyDirectoriesRegularFiles(3, 1)
                .withDirectory("aa", dir1 ->
                        dir1.hasOnlyDirectories(1)
                            .withDirectory("bb", dir2 ->
                                    dir2.hasOnlyRegularFiles(2)
                                        .withRegularFile("group.zip", file -> file.hasSize(763))
                                        .withRegularFile("group_group.txt", file -> file.hasSize(11))))
                .withDirectory("one_two", dir1 ->
                        dir1.hasOnlyDirectoriesRegularFiles(2, 1)
                            .withDirectory("one", dir2 ->
                                    dir2.hasOnlyRegularFiles(2)
                                        .withRegularFile("one.txt", file -> file.hasSize(1))
                                        .withRegularFile("one_one.txt", file -> file.hasSize(3)))
                            .withDirectory("two", dir2 ->
                                    dir2.hasOnlyRegularFiles(2)
                                        .withRegularFile("two.txt", file -> file.hasSize(1))
                                        .withRegularFile("two_two.txt", file -> file.hasSize(3)))
                            .withRegularFile("one_two.txt", file -> file.hasSize(3)))
                .withDirectory("three_four", dir1 ->
                        dir1.hasOnlyDirectoriesRegularFiles(2, 1)
                            .withDirectory("four", dir2 ->
                                    dir2.hasOnlyRegularFiles(2)
                                        .withRegularFile("four.txt", file -> file.hasSize(1))
                                        .withRegularFile("four_four.txt", file -> file.hasSize(3)))
                            .withDirectory("three", dir2 ->
                                    dir2.hasOnlyRegularFiles(2)
                                        .withRegularFile("three.txt", file -> file.hasSize(1))
                                        .withRegularFile("three_three.txt", file -> file.hasSize(3)))
                            .withRegularFile("three_four.txt", file -> file.hasSize(3)))
                .withRegularFile("one_two_three_four.txt", file -> file.hasSize(7));
    }

    /**
     * When we set {@code recursiveLevel = 3}, only up to 3rd level zip files
     * should be extracted. In the current example, there are:
     * <ul>
     * <li>{@code one_two.zip} as 1st level</li>
     * <li>{@code three_four.zip} as 1st level</li>
     * <li>{@code one.zip} as 2nd level</li>
     * <li>{@code two.zip} as 2nd level</li>
     * <li>{@code three.zip} as 2nd level</li>
     * <li>{@code four.zip} as 2nd level</li>
     * <li>{@code five_six.zip} as 3rd level</li>
     * <li>{@code seven_eight.zip} as 3rd level</li>
     * <li>{@code seven_eight.zip} as 3rd level</li>
     * <li>{@code seven_eight.zip} as 3rd level</li>
     * </ul>
     * As result, we should have the following content on the disc.
     * <pre>
     * >recursive
     * > |-- aa
     * > |    |-- bb
     * > |    |    |-- group.zip
     * > |    |    |-- group_group.txt
     * > |-- one_two
     * > |    |-- one
     * > |    |    |-- one.txt
     * > |    |    |-- one_one.txt
     * > |    |-- two
     * > |    |    |-- two.txt
     * > |    |    |-- tow_two.txt
     * > |    |-- one_two.txt
     * > |-- three_four
     * > |    |-- three
     * > |    |    |-- three.txt
     * > |    |    |-- three_three.txt
     * > |    |-- four
     * > |    |    |-- four.txt
     * > |    |    |-- four_four.txt
     * > |    |-- three_four.txt
     * </pre>
     */
    public void shouldUnzipUpToThirdLevelWhenRecursiveLevelThree() {
        Path dstDir = getTestRoot();
        UnzipSettings settings = UnzipSettings.builder().recursiveLevel(3).build();

        UnzipIt.zip(zip).settings(settings).dstDir(dstDir).extract();

        assertThatDirectory(dstDir)
                .hasOnlyDirectoriesRegularFiles(3, 1)
                .withDirectory("aa", dir1 ->
                        dir1.hasOnlyDirectories(1)
                            .withDirectory("bb", dir2 ->
                                    dir2.hasOnlyDirectoriesRegularFiles(1, 1)
                                        .withDirectory("group", dir3 ->
                                                dir3.hasOnlyRegularFiles(3)
                                                    .withRegularFile("five_six.zip", file -> file.hasSize(282))
                                                    .withRegularFile("seven_eight.zip", file -> file.hasSize(360))
                                                    .withRegularFile("five_six_seven_eight.txt",
                                                                     file -> file.hasSize(7)))
                                        .withRegularFile("group_group.txt", file -> file.hasSize(11))))
                .withDirectory("one_two", dir1 ->
                        dir1.hasOnlyDirectoriesRegularFiles(2, 1)
                            .withDirectory("one", dir2 ->
                                    dir2.hasOnlyRegularFiles(2)
                                        .withRegularFile("one.txt", file -> file.hasSize(1))
                                        .withRegularFile("one_one.txt", file -> file.hasSize(3)))
                            .withDirectory("two", dir2 ->
                                    dir2.hasOnlyRegularFiles(2)
                                        .withRegularFile("two.txt", file -> file.hasSize(1))
                                        .withRegularFile("two_two.txt", file -> file.hasSize(3)))
                            .withRegularFile("one_two.txt", file -> file.hasSize(3)))
                .withDirectory("three_four", dir1 ->
                        dir1.hasOnlyDirectoriesRegularFiles(2, 1)
                            .withDirectory("four", dir2 ->
                                    dir2.hasOnlyRegularFiles(2)
                                        .withRegularFile("four.txt", file -> file.hasSize(1))
                                        .withRegularFile("four_four.txt", file -> file.hasSize(3)))
                            .withDirectory("three", dir2 ->
                                    dir2.hasOnlyRegularFiles(2)
                                        .withRegularFile("three.txt", file -> file.hasSize(1))
                                        .withRegularFile("three_three.txt", file -> file.hasSize(3)))
                            .withRegularFile("three_four.txt", file -> file.hasSize(3)))
                .withRegularFile("one_two_three_four.txt", file -> file.hasSize(7));
    }

    /**
     * When we set {@code recursiveLevel = 4}, only up to 4th level zip files
     * should be extracted. In the current example, there are:
     * <ul>
     * <li>{@code one_two.zip} as 1st level</li>
     * <li>{@code three_four.zip} as 1st level</li>
     * <li>{@code one.zip} as 2nd level</li>
     * <li>{@code two.zip} as 2nd level</li>
     * <li>{@code three.zip} as 2nd level</li>
     * <li>{@code four.zip} as 2nd level</li>
     * <li>{@code five_six.zip} as 3rd level</li>
     * <li>{@code seven_eight.zip} as 3rd level</li>
     * <li>{@code seven_eight.zip} as 3rd level</li>
     * <li>{@code seven_eight.zip} as 3rd level</li>
     * </ul>
     * As result, we should have the following content on the disc.
     * <pre>
     * >recursive
     * > |-- aa
     * > |    |-- bb
     * > |    |    |-- group.zip
     * > |    |    |-- group_group.txt
     * > |-- one_two
     * > |    |-- one
     * > |    |    |-- one.txt
     * > |    |    |-- one_one.txt
     * > |    |-- two
     * > |    |    |-- two.txt
     * > |    |    |-- tow_two.txt
     * > |    |-- one_two.txt
     * > |-- three_four
     * > |    |-- three
     * > |    |    |-- three.txt
     * > |    |    |-- three_three.txt
     * > |    |-- four
     * > |    |    |-- four.txt
     * > |    |    |-- four_four.txt
     * > |    |-- three_four.txt
     * </pre>
     */
    public void shouldUnzipUpToFourthLevelWhenRecursiveLevelFour() {
        Path dstDir = getTestRoot();
        UnzipSettings settings = UnzipSettings.builder().recursiveLevel(4).build();

        UnzipIt.zip(zip).settings(settings).dstDir(dstDir).extract();

        assertThatDirectory(dstDir)
                .hasOnlyDirectoriesRegularFiles(3, 1)
                .withDirectory("aa", dir2 ->
                        dir2.hasOnlyDirectories(1)
                            .withDirectory("bb", dir3 ->
                                    dir3.hasOnlyDirectoriesRegularFiles(1, 1)
                                        .withDirectory("group", dir4 ->
                                                dir4.hasOnlyDirectoriesRegularFiles(2, 1)
                                                    .withDirectory("five_six", dir5 ->
                                                            dir5.hasOnlyRegularFiles(2)
                                                                .withRegularFile("five.txt", file -> file.hasSize(1))
                                                                .withRegularFile("six.txt", file -> file.hasSize(1)))
                                                    .withDirectory("seven_eight", dir5 ->
                                                            dir5.hasOnlyRegularFiles(2)
                                                                .withRegularFile("eight.txt", file -> file.hasSize(1))
                                                                .withRegularFile("seven.txt", file -> file.hasSize(1)))
                                                    .withRegularFile("five_six_seven_eight.txt",
                                                                     file -> file.hasSize(7)))
                                        .withRegularFile("group_group.txt", file -> file.hasSize(11))))
                .withDirectory("one_two", dir2 ->
                        dir2.hasOnlyDirectoriesRegularFiles(2, 1)
                            .withDirectory("one", dir3 ->
                                    dir3.hasOnlyRegularFiles(2)
                                        .withRegularFile("one.txt", file -> file.hasSize(1))
                                        .withRegularFile("one_one.txt", file -> file.hasSize(3)))
                            .withDirectory("two", dir3 ->
                                    dir3.hasOnlyRegularFiles(2)
                                        .withRegularFile("two.txt", file -> file.hasSize(1))
                                        .withRegularFile("two_two.txt", file -> file.hasSize(3)))
                            .withRegularFile("one_two.txt", file -> file.hasSize(3)))
                .withDirectory("three_four", dir2 ->
                        dir2.hasOnlyDirectoriesRegularFiles(2, 1)
                            .withDirectory("four", dir3 ->
                                    dir3.hasOnlyRegularFiles(2)
                                        .withRegularFile("four.txt", file -> file.hasSize(1))
                                        .withRegularFile("four_four.txt", file -> file.hasSize(3)))
                            .withDirectory("three", dir3 ->
                                    dir3.hasOnlyRegularFiles(2)
                                        .withRegularFile("three.txt", file -> file.hasSize(1))
                                        .withRegularFile("three_three.txt", file -> file.hasSize(3)))
                            .withRegularFile("three_four.txt", file -> file.hasSize(3)))
                .withRegularFile("one_two_three_four.txt", file -> file.hasSize(7));
    }

    public void shouldUnzipUpToMaxLevelWhenRecursiveLevelMax() {
        Path dstDir = getTestRoot();
        UnzipSettings settings = UnzipSettings.builder().recursiveLevelMax().build();

        UnzipIt.zip(zip).settings(settings).dstDir(dstDir).extract();

        assertThatDirectory(dstDir)
                .hasOnlyDirectoriesRegularFiles(3, 1)
                .withDirectory("aa", dir2 ->
                        dir2.hasOnlyDirectories(1)
                            .withDirectory("bb", dir3 ->
                                    dir3.hasOnlyDirectoriesRegularFiles(1, 1)
                                        .withDirectory("group", dir4 ->
                                                dir4.hasOnlyDirectoriesRegularFiles(2, 1)
                                                    .withDirectory("five_six", dir5 ->
                                                            dir5.hasOnlyRegularFiles(2)
                                                                .withRegularFile("five.txt", file -> file.hasSize(1))
                                                                .withRegularFile("six.txt", file -> file.hasSize(1)))
                                                    .withDirectory("seven_eight", dir5 ->
                                                            dir5.hasOnlyRegularFiles(2)
                                                                .withRegularFile("eight.txt", file -> file.hasSize(1))
                                                                .withRegularFile("seven.txt", file -> file.hasSize(1)))
                                                    .withRegularFile("five_six_seven_eight.txt",
                                                                     file -> file.hasSize(7)))
                                        .withRegularFile("group_group.txt", file -> file.hasSize(11))))
                .withDirectory("one_two", dir2 ->
                        dir2.hasOnlyDirectoriesRegularFiles(2, 1)
                            .withDirectory("one", dir3 ->
                                    dir3.hasOnlyRegularFiles(2)
                                        .withRegularFile("one.txt", file -> file.hasSize(1))
                                        .withRegularFile("one_one.txt", file -> file.hasSize(3)))
                            .withDirectory("two", dir3 ->
                                    dir3.hasOnlyRegularFiles(2)
                                        .withRegularFile("two.txt", file -> file.hasSize(1))
                                        .withRegularFile("two_two.txt", file -> file.hasSize(3)))
                            .withRegularFile("one_two.txt", file -> file.hasSize(3)))
                .withDirectory("three_four", dir2 ->
                        dir2.hasOnlyDirectoriesRegularFiles(2, 1)
                            .withDirectory("four", dir3 ->
                                    dir3.hasOnlyRegularFiles(2)
                                        .withRegularFile("four.txt", file -> file.hasSize(1))
                                        .withRegularFile("four_four.txt", file -> file.hasSize(3)))
                            .withDirectory("three", dir3 ->
                                    dir3.hasOnlyRegularFiles(2)
                                        .withRegularFile("three.txt", file -> file.hasSize(1))
                                        .withRegularFile("three_three.txt", file -> file.hasSize(3)))
                            .withRegularFile("three_four.txt", file -> file.hasSize(3)))
                .withRegularFile("one_two_three_four.txt", file -> file.hasSize(7));
    }

}
