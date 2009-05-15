package org.springframework.roo.addon.maven;

import java.io.InputStream;

import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.model.JavaPackage;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.process.manager.MutableFile;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.PathResolver;
import org.springframework.roo.project.ProjectMetadata;
import org.springframework.roo.project.ProjectMetadataProvider;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.support.lifecycle.ScopeDevelopment;
import org.springframework.roo.support.util.Assert;
import org.springframework.roo.support.util.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Provides Maven project operations. 
 * 
 * @author Ben Alex
 * @since 1.0
 *
 */
@ScopeDevelopment
public class MavenOperations extends ProjectOperations {
	private FileManager fileManager;
	private PathResolver pathResolver;
	private MetadataService metadataService;
	
	public MavenOperations(MetadataService metadataService, ProjectMetadataProvider projectMetadataProvider, FileManager fileManager, PathResolver pathResolver) {
		super(metadataService, projectMetadataProvider);
		Assert.notNull(metadataService, "Metadata service required");
		Assert.notNull(fileManager, "File manager required");
		Assert.notNull(pathResolver, "Path resolver required");
		this.metadataService = metadataService;
		this.fileManager = fileManager;
		this.pathResolver = pathResolver;
	}
	
	public boolean isCreateProjectAvailable() {
		return metadataService.get(ProjectMetadata.getProjectIdentifier()) == null;
	}
	
	public void createProject(InputStream templateInputStream, JavaPackage topLevelPackage, String projectName) {
		Assert.isTrue(isCreateProjectAvailable(), "Project creation is unavailable at this time");
		Assert.notNull(templateInputStream, "Could not acquire template POM");
		Assert.notNull(topLevelPackage, "Top level package required");
		Assert.hasText(projectName, "Project name required");
		
		Document pom;
		try {
			pom = XmlUtils.getDocumentBuilder().parse(templateInputStream);
		} catch (Exception ex) {
			throw new IllegalStateException(ex);
		}

		Element rootElement = (Element) pom.getFirstChild();
		XmlUtils.findRequiredElement("//artifactId", rootElement).setTextContent(projectName);
		XmlUtils.findRequiredElement("//groupId", rootElement).setTextContent(topLevelPackage.getFullyQualifiedPackageName());
		XmlUtils.findRequiredElement("//name", rootElement).setTextContent(projectName);
		
		MutableFile pomMutableFile = fileManager.createFile(pathResolver.getIdentifier(Path.ROOT, "pom.xml"));
		XmlUtils.writeXml(pomMutableFile.getOutputStream(), pom);
		
		fileManager.scanAll();
	}
	
}
