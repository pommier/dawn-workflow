/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package org.dawb.passerelle.dnd;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.dawb.common.util.io.FileUtils;
import org.dawb.gda.extensions.loaders.H5Loader;
import org.dawb.passerelle.actors.data.DataImportSource;
import org.dawb.passerelle.actors.data.FolderImportSource;
import org.dawb.passerelle.actors.data.SpecImportSource;
import org.dawb.passerelle.actors.scripts.PythonScript;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.kernel.util.NamedObj;
import uk.ac.diamond.scisoft.analysis.io.IMetaData;
import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;

import com.isencia.passerelle.workbench.model.editor.ui.dnd.IDropClassFactory;
import com.isencia.passerelle.workbench.model.ui.command.CreateComponentCommand;

public class DawbDropFactory implements IDropClassFactory {

	private static Logger logger = LoggerFactory.getLogger(DawbDropFactory.class);
	
	private final Map<String, Class<? extends NamedObj>>  classes;
	public DawbDropFactory() {
		classes = new HashMap<String, Class<? extends NamedObj>>(7);
		final Collection<String> exts = LoaderFactory.getSupportedExtensions();
		for (String ext : exts) {
			classes.put(ext, DataImportSource.class);
		}
		classes.put("py",   PythonScript.class);
		classes.put("jy",   PythonScript.class);
		classes.put("spec", SpecImportSource.class);
	}

	@Override
	public Class<? extends NamedObj> getClassForPath(final IResource source, String filePath) {

		if (filePath==null) return null;
		if (source!=null && source instanceof IContainer) return FolderImportSource.class;
		final File  file = new File(filePath);
		if (file.isDirectory()) return FolderImportSource.class;
		
        final String ext = FileUtils.getFileExtension(file);
        Class<? extends NamedObj> clazz = classes.get(ext);
        
        if (clazz == null) clazz = DataImportSource.class;
        
        return clazz;
        
	}

	@Override
	public void setConfigurableParameters(CreateComponentCommand cmd, String filePath) {

		if (H5Loader.isH5(filePath)) {

			IMetaData data;
			try {
				data = LoaderFactory.getMetaData(filePath, null);
			} catch (Exception e) {
				logger.error("Cannot get meta data for "+filePath, e);
				return;
			}
			if (data!=null&&data.getDataNames()!=null&&data.getDataNames().size()==1) {
				cmd.addConfigurableParameterValue("Data Sets", data.getDataNames().iterator().next());
			}
		}

	}

}
