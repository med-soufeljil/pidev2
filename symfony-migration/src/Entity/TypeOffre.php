<?php

namespace App\Entity;

enum TypeOffre: string
{
    case CDI = 'CDI';
    case CDD = 'CDD';
    case STAGE = 'STAGE';
    case FREELANCE = 'FREELANCE';
}
