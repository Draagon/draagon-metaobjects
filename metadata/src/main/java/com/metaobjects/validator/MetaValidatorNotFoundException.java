/*
 * Copyright 2003 Doug Mealing LLC dba Meta Objects. All Rights Reserved.
 *
 * This software is the proprietary information of Doug Mealing LLC dba Meta Objects.
 * Use is subject to license terms.
 */

package com.metaobjects.validator;

import com.metaobjects.MetaDataNotFoundException;

public class MetaValidatorNotFoundException
  extends MetaDataNotFoundException
{
  public MetaValidatorNotFoundException( String msg, String name )
  {
    super( msg, name );
  }
}

